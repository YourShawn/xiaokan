package com.xiaokan.vision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaokan.config.XiaokanProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VisionConfig {

    @Bean
    public VisionClient visionClient(XiaokanProperties properties, ObjectMapper objectMapper) {
        if (properties.isMockMode()) {
            return new MockVisionClient();
        }
        return new OpenAiVisionClient(properties, objectMapper);
    }
}
