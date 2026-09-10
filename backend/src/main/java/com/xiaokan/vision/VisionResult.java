package com.xiaokan.vision;

import java.util.ArrayList;
import java.util.List;

public record VisionResult(
        String brand,
        String category,
        String fabric,
        String sku,
        java.math.BigDecimal listPrice,
        boolean priceVisible,
        boolean tagReadable,
        boolean careLabelReadable,
        boolean insufficientEvidence,
        String retakeHint,
        Integer bargainScore,
        List<String> reasons
) {
    public VisionResult {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static VisionResult insufficient(String hint) {
        return new VisionResult(
                null, null, null, null, null,
                false, false, false, true, hint, null,
                List.of(hint)
        );
    }

    public VisionResult withReasonPrefix(String extra) {
        List<String> next = new ArrayList<>();
        next.add(extra);
        next.addAll(reasons);
        return new VisionResult(
                brand, category, fabric, sku, listPrice,
                priceVisible, tagReadable, careLabelReadable,
                insufficientEvidence, retakeHint, bargainScore, next
        );
    }
}
