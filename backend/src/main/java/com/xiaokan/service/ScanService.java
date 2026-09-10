package com.xiaokan.service;

import com.xiaokan.config.XiaokanProperties;
import com.xiaokan.domain.Scan;
import com.xiaokan.domain.ScanPhoto;
import com.xiaokan.repo.ScanRepository;
import com.xiaokan.storage.PhotoStorage;
import com.xiaokan.vision.VisionClient;
import com.xiaokan.vision.VisionResult;
import com.xiaokan.web.ApiException;
import com.xiaokan.web.dto.DealRequest;
import com.xiaokan.web.dto.ScanResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

@Service
public class ScanService {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif", "image/heic", "image/heif"
    );

    private final ScanRepository scans;
    private final VisionClient visionClient;
    private final PhotoStorage photoStorage;
    private final XiaokanProperties properties;

    public ScanService(
            ScanRepository scans,
            VisionClient visionClient,
            PhotoStorage photoStorage,
            XiaokanProperties properties
    ) {
        this.scans = scans;
        this.visionClient = visionClient;
        this.photoStorage = photoStorage;
        this.properties = properties;
    }

    @Transactional
    public ScanResponse create(List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty() || photos.size() > 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "请拍 1～3 张照片（衣服 / 价签 / 水洗标）");
        }
        for (MultipartFile photo : photos) {
            if (photo.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "有一张照片是空的，再拍一次吧");
            }
            String type = photo.getContentType();
            if (type != null && !IMAGE_TYPES.contains(type.toLowerCase()) && !type.startsWith("image/")) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "只接受照片图片");
            }
        }

        VisionResult vision = visionClient.analyze(photos);
        BargainAdvisor.Suggestion suggestion = BargainAdvisor.suggest(vision);

        Scan scan = new Scan();
        scan.setBrand(vision.brand());
        scan.setCategory(vision.category());
        scan.setFabric(vision.fabric());
        scan.setSku(vision.sku());
        scan.setListPrice(vision.listPrice());
        scan.setOpeningOffer(suggestion.openingOffer());
        scan.setTargetMin(suggestion.targetMin());
        scan.setTargetMax(suggestion.targetMax());
        scan.setMaxPrice(suggestion.maxPrice());
        scan.setBargainScore(suggestion.bargainScore());
        scan.setReasons(vision.reasons());
        scan.setNeedsRetakeTag(suggestion.needsRetakeTag());
        scan.setRetakeHint(suggestion.retakeHint());
        scan.setInsufficientEvidence(vision.insufficientEvidence());
        scan.setMockMode(properties.isMockMode());
        scan = scans.save(scan);

        try {
            int index = 0;
            for (MultipartFile photo : photos) {
                PhotoStorage.StoredPhoto stored = photoStorage.save(scan.getId(), index, photo);
                ScanPhoto row = new ScanPhoto();
                row.setSortOrder(index);
                row.setOriginalFilename(photo.getOriginalFilename());
                row.setContentType(photo.getContentType());
                row.setStoragePath(stored.absolutePath());
                row.setSizeBytes(stored.sizeBytes());
                scan.addPhoto(row);
                index++;
            }
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "照片保存失败");
        }

        return ScanResponse.from(scans.save(scan));
    }

    @Transactional(readOnly = true)
    public List<ScanResponse> history() {
        return scans.findAllByOrderByCreatedAtDesc().stream().map(ScanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ScanResponse get(Long id) {
        return ScanResponse.from(load(id));
    }

    @Transactional
    public ScanResponse recordDeal(Long id, DealRequest request) {
        Scan scan = load(id);
        if (Boolean.TRUE.equals(request.bought())) {
            if (request.finalPrice() == null || request.finalPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "拿下了的话，告诉小砍最终成交价呀");
            }
            scan.setBought(true);
            scan.setFinalPrice(request.finalPrice());
        } else {
            scan.setBought(false);
            scan.setFinalPrice(null);
        }
        return ScanResponse.from(scans.save(scan));
    }

    @Transactional(readOnly = true)
    public PhotoFile loadPhoto(Long scanId, Long photoId) {
        Scan scan = load(scanId);
        ScanPhoto photo = scan.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "找不到这张照片"));
        try {
            byte[] bytes = Files.readAllBytes(photoStorage.resolve(photo.getStoragePath()));
            String type = photo.getContentType() == null ? "image/jpeg" : photo.getContentType();
            return new PhotoFile(bytes, type);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND.value(), "照片文件不见了");
        }
    }

    private Scan load(Long id) {
        return scans.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "找不到这次扫描"));
    }

    public record PhotoFile(byte[] bytes, String contentType) {
    }
}
