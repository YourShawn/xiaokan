package com.xiaokan.service;

import com.xiaokan.vision.VisionResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BargainAdvisor {

    private BargainAdvisor() {
    }

    public static Suggestion suggest(VisionResult vision) {
        boolean needsRetake = !vision.tagReadable() && !vision.insufficientEvidence();
        String hint = vision.retakeHint();
        if (needsRetake && (hint == null || hint.isBlank())) {
            hint = "价签再拍一张会更准哦，也可以先跳过。";
        }

        if (vision.insufficientEvidence() || vision.listPrice() == null) {
            return new Suggestion(null, null, null, null, vision.bargainScore(), needsRetake, hint);
        }

        BigDecimal list = vision.listPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal opening = yuan(list, "0.55");
        BigDecimal targetMin = yuan(list, "0.62");
        BigDecimal targetMax = yuan(list, "0.78");
        BigDecimal max = yuan(list, "0.88");
        if (opening.compareTo(targetMin) >= 0) {
            opening = targetMin.subtract(BigDecimal.ONE).max(BigDecimal.ONE);
        }
        if (max.compareTo(list) >= 0) {
            max = list.subtract(BigDecimal.ONE).max(targetMax);
        }
        int score = vision.bargainScore() == null ? defaultScore(list) : clampScore(vision.bargainScore());
        return new Suggestion(opening, targetMin, targetMax, max, score, needsRetake, hint);
    }

    private static BigDecimal yuan(BigDecimal list, String ratio) {
        return list.multiply(new BigDecimal(ratio)).setScale(0, RoundingMode.HALF_UP);
    }

    private static int defaultScore(BigDecimal list) {
        if (list.compareTo(new BigDecimal("150")) <= 0) {
            return 82;
        }
        if (list.compareTo(new BigDecimal("400")) <= 0) {
            return 74;
        }
        return 58;
    }

    private static int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    public record Suggestion(
            BigDecimal openingOffer,
            BigDecimal targetMin,
            BigDecimal targetMax,
            BigDecimal maxPrice,
            Integer bargainScore,
            boolean needsRetakeTag,
            String retakeHint
    ) {
    }
}
