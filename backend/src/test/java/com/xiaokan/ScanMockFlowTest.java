package com.xiaokan;

import com.xiaokan.web.dto.ScanResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScanMockFlowTest {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) throws Exception {
        registry.add("xiaokan.upload-dir", () -> {
            try {
                return Files.createTempDirectory("xiaokan-test").toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        registry.add("xiaokan.openai.api-key", () -> "");
    }

    @Autowired
    TestRestTemplate http;

    @Test
    void mockScanThenDealPersistsSavedAmount() throws Exception {
        byte[] png = pngBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("photos", new NamedPng("shirt.png", png));
        body.add("photos", new NamedPng("price-tag.png", png));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<ScanResponse> created = http.postForEntity(
                "/api/scans", new HttpEntity<>(body, headers), ScanResponse.class);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        ScanResponse scan = created.getBody();
        assertThat(scan).isNotNull();
        assertThat(scan.mockMode()).isTrue();
        assertThat(scan.listPrice()).isNotNull();
        assertThat(scan.openingOffer()).isNotNull();
        assertThat(scan.reasons()).isNotEmpty();

        ResponseEntity<ScanResponse> dealt = http.postForEntity(
                "/api/scans/" + scan.id() + "/deal",
                Map.of("bought", true, "final_price", 79),
                ScanResponse.class);
        assertThat(dealt.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(dealt.getBody()).isNotNull();
        assertThat(dealt.getBody().bought()).isTrue();
        assertThat(dealt.getBody().savedAmount()).isNotNull();
        assertThat(dealt.getBody().savedAmount()).isPositive();
    }

    private static byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 320; x++) {
            for (int y = 0; y < 320; y++) {
                image.setRGB(x, y, (x * 7 + y * 13) & 0xFFFFFF);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    static class NamedPng extends ByteArrayResource {
        private final String name;

        NamedPng(String name, byte[] bytes) {
            super(bytes);
            this.name = name;
        }

        @Override
        public String getFilename() {
            return name;
        }
    }
}
