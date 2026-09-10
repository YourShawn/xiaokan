package com.xiaokan.service;

import com.xiaokan.vision.VisionResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BargainAdvisorTest {

    @Test
    void derivesOffersBelowListPrice() {
        VisionResult vision = new VisionResult(
                "优衣库", "T恤", "棉", "1",
                new BigDecimal("199.00"),
                true, true, false, false, null, 70,
                List.of("价签清晰")
        );
        BargainAdvisor.Suggestion suggestion = BargainAdvisor.suggest(vision);
        assertThat(suggestion.openingOffer()).isLessThan(suggestion.targetMin());
        assertThat(suggestion.targetMin()).isLessThanOrEqualTo(suggestion.targetMax());
        assertThat(suggestion.targetMax()).isLessThanOrEqualTo(suggestion.maxPrice());
        assertThat(suggestion.maxPrice()).isLessThan(new BigDecimal("199.00"));
        assertThat(suggestion.needsRetakeTag()).isFalse();
    }

    @Test
    void doesNotInventOffersWithoutListPrice() {
        VisionResult vision = new VisionResult(
                "未知", "外套", null, null,
                null, false, false, false, false,
                "再拍价签", null, List.of("未见价签")
        );
        BargainAdvisor.Suggestion suggestion = BargainAdvisor.suggest(vision);
        assertThat(suggestion.openingOffer()).isNull();
        assertThat(suggestion.maxPrice()).isNull();
        assertThat(suggestion.needsRetakeTag()).isTrue();
    }
}
