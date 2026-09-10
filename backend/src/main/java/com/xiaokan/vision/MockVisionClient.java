package com.xiaokan.vision;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Used when OPENAI_API_KEY is empty. Returns a labeled demo payload so the
 * E2E path works; UI must show 模拟. Live vision never uses this class.
 */
public class MockVisionClient implements VisionClient {

    static final long MIN_USEFUL_BYTES = 2_048;

    @Override
    public VisionResult analyze(List<MultipartFile> photos) {
        long usefulBytes = photos.stream().mapToLong(MultipartFile::getSize).sum();
        if (photos.isEmpty() || usefulBytes < MIN_USEFUL_BYTES) {
            return VisionResult.insufficient("照片太糊或太小，小砍没看清衣服。再拍一张近一点的？");
        }

        boolean likelyHasTag = photos.size() >= 2
                || photos.stream().anyMatch(MockVisionClient::filenameLooksLikeTag);
        String retakeHint = likelyHasTag
                ? null
                : "价签再拍一张会更准哦，也可以先跳过看建议。";

        BigDecimal listPrice = likelyHasTag ? new BigDecimal("199.00") : new BigDecimal("99.00");

        return new VisionResult(
                "优衣库",
                "圆领T恤",
                "100%棉",
                likelyHasTag ? "455123" : null,
                listPrice,
                true,
                likelyHasTag,
                photos.size() >= 3,
                false,
                retakeHint,
                likelyHasTag ? 74 : 80,
                List.of(
                        "模拟模式：没有配置视觉 API Key，价签数字来自演示目录。",
                        likelyHasTag ? "按棉质基础款的常见活动价，开口可以低一点。" : "只看到衣服本体，价签建议按基础款演示标价。",
                        "真实现场请以店内价签为准。"
                )
        );
    }

    static boolean filenameLooksLikeTag(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return lower.contains("tag") || lower.contains("price") || lower.contains("价");
    }
}
