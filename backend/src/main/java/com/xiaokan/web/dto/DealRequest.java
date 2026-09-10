package com.xiaokan.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DealRequest(
        @NotNull Boolean bought,
        BigDecimal finalPrice
) {
}
