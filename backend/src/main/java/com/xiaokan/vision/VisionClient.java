package com.xiaokan.vision;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VisionClient {
    VisionResult analyze(List<MultipartFile> photos);
}
