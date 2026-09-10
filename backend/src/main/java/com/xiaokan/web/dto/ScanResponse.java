package com.xiaokan.web.dto;

import com.xiaokan.domain.Scan;
import com.xiaokan.domain.ScanPhoto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ScanResponse(
        Long id,
        Instant createdAt,
        String brand,
        String category,
        String fabric,
        String sku,
        BigDecimal listPrice,
        BigDecimal openingOffer,
        BigDecimal targetMin,
        BigDecimal targetMax,
        BigDecimal maxPrice,
        Integer bargainScore,
        List<String> reasons,
        boolean needsRetakeTag,
        String retakeHint,
        boolean insufficientEvidence,
        boolean mockMode,
        Boolean bought,
        BigDecimal finalPrice,
        BigDecimal savedAmount,
        List<PhotoResponse> photos
) {
    public static ScanResponse from(Scan scan) {
        List<PhotoResponse> photos = scan.getPhotos().stream()
                .map(p -> PhotoResponse.from(scan.getId(), p))
                .toList();
        return new ScanResponse(
                scan.getId(),
                scan.getCreatedAt(),
                scan.getBrand(),
                scan.getCategory(),
                scan.getFabric(),
                scan.getSku(),
                scan.getListPrice(),
                scan.getOpeningOffer(),
                scan.getTargetMin(),
                scan.getTargetMax(),
                scan.getMaxPrice(),
                scan.getBargainScore(),
                scan.getReasons(),
                scan.isNeedsRetakeTag(),
                scan.getRetakeHint(),
                scan.isInsufficientEvidence(),
                scan.isMockMode(),
                scan.getBought(),
                scan.getFinalPrice(),
                scan.savedAmount(),
                photos
        );
    }

    public record PhotoResponse(Long id, int sortOrder, String url, String contentType) {
        static PhotoResponse from(Long scanId, ScanPhoto photo) {
            return new PhotoResponse(
                    photo.getId(),
                    photo.getSortOrder(),
                    "/api/scans/" + scanId + "/photos/" + photo.getId(),
                    photo.getContentType()
            );
        }
    }
}
