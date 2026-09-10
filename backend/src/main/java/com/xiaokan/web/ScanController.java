package com.xiaokan.web;

import com.xiaokan.config.XiaokanProperties;
import com.xiaokan.service.ScanService;
import com.xiaokan.web.dto.DealRequest;
import com.xiaokan.web.dto.ScanResponse;
import com.xiaokan.web.dto.StatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final ScanService scanService;
    private final XiaokanProperties properties;

    public ScanController(ScanService scanService, XiaokanProperties properties) {
        this.scanService = scanService;
        this.properties = properties;
    }

    @GetMapping("/status")
    public StatusResponse status() {
        return new StatusResponse(properties.isMockMode(), "小砍");
    }

    @PostMapping(path = "/scans", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ScanResponse create(@RequestParam("photos") List<MultipartFile> photos) {
        return scanService.create(photos);
    }

    @GetMapping("/scans")
    public List<ScanResponse> history() {
        return scanService.history();
    }

    @GetMapping("/scans/{id}")
    public ScanResponse get(@PathVariable Long id) {
        return scanService.get(id);
    }

    @PostMapping("/scans/{id}/deal")
    public ScanResponse deal(@PathVariable Long id, @Valid @RequestBody DealRequest request) {
        return scanService.recordDeal(id, request);
    }

    @GetMapping("/scans/{id}/photos/{photoId}")
    public ResponseEntity<byte[]> photo(@PathVariable Long id, @PathVariable Long photoId) {
        ScanService.PhotoFile file = scanService.loadPhoto(id, photoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.bytes());
    }
}
