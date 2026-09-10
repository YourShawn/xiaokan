package com.xiaokan.vision;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockVisionClientTest {

    private final MockVisionClient client = new MockVisionClient();

    @Test
    void tinyPhotoIsInsufficientAndHasNoPrice() {
        MockMultipartFile tiny = new MockMultipartFile("photos", "x.jpg", "image/jpeg", new byte[10]);
        VisionResult result = client.analyze(List.of(tiny));
        assertThat(result.insufficientEvidence()).isTrue();
        assertThat(result.listPrice()).isNull();
    }

    @Test
    void usefulPhotosReturnLabeledDemo() {
        byte[] bytes = new byte[4096];
        MockMultipartFile garment = new MockMultipartFile("photos", "shirt.jpg", "image/jpeg", bytes);
        MockMultipartFile tag = new MockMultipartFile("photos", "price-tag.jpg", "image/jpeg", bytes);
        VisionResult result = client.analyze(List.of(garment, tag));
        assertThat(result.insufficientEvidence()).isFalse();
        assertThat(result.listPrice()).isNotNull();
        assertThat(result.reasons().get(0)).contains("模拟");
    }
}
