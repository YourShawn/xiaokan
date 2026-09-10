package com.xiaokan.vision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaokan.config.XiaokanProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiVisionClientTest {

    @Test
    void ignoresPriceWhenNotVisible() throws Exception {
        XiaokanProperties properties = new XiaokanProperties();
        OpenAiVisionClient client = new OpenAiVisionClient(properties, new ObjectMapper());
        String raw = """
                {"choices":[{"message":{"content":"{\"brand\":\"A\",\"list_price\":999,\"price_visible\":false,\"tag_readable\":false,\"insufficient\":false,\"reasons\":[]}"}}]}
                """;
        VisionResult result = client.parse(raw);
        assertThat(result.listPrice()).isNull();
        assertThat(result.brand()).isEqualTo("A");
    }

    @Test
    void keepsPriceOnlyWhenVisible() throws Exception {
        XiaokanProperties properties = new XiaokanProperties();
        OpenAiVisionClient client = new OpenAiVisionClient(properties, new ObjectMapper());
        String raw = """
                {"choices":[{"message":{"content":"{\"brand\":\"B\",\"list_price\":128,\"price_visible\":true,\"tag_readable\":true,\"insufficient\":false,\"reasons\":[\"价签清晰\"]}"}}]}
                """;
        VisionResult result = client.parse(raw);
        assertThat(result.listPrice()).isEqualByComparingTo("128");
    }
}
