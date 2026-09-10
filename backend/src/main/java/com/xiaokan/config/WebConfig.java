package com.xiaokan.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(XiaokanProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final XiaokanProperties properties;

    public WebConfig(XiaokanProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] patterns = properties.getCorsAllowedOriginPatterns().split("\\s*,\\s*");
        registry.addMapping("/api/**")
                .allowedOriginPatterns(patterns)
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }
}
