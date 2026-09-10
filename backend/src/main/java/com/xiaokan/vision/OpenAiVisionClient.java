package com.xiaokan.vision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaokan.config.XiaokanProperties;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAiVisionClient implements VisionClient {

    private static final String SYSTEM = """
            你是服装价签/水洗标视觉助手。只根据图片里能看清的文字与图案作答。
            严禁编造价签价格：看不清就 list_price 必须为 null，price_visible 为 false。
            不要猜测品牌、SKU、面料；看不清就填 null。
            只返回 JSON，不要 markdown。
            """;

    private static final String USER = """
            这些照片可能是：衣服本体、价签、水洗标。请提取：
            brand, category, fabric, sku, list_price（数字或 null）,
            price_visible, tag_readable, care_label_readable,
            insufficient（完全看不出是衣服则为 true）,
            retake_hint（中文，价签不清时提示补拍，可空）,
            bargain_score（0-100，按品牌定位/面料能否砍价，看不清则 null）,
            reasons（中文短句数组，解释依据）。
            """;

    private final XiaokanProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiVisionClient(XiaokanProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(trimSlash(properties.getOpenai().getBaseUrl()))
                .build();
    }

    @Override
    public VisionResult analyze(List<MultipartFile> photos) {
        try {
            Map<String, Object> body = chatBody(photos);
            String raw = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getOpenai().getApiKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parse(raw);
        } catch (Exception ex) {
            return VisionResult.insufficient("视觉服务暂时不可用，稍后再拍一次。").withReasonPrefix(ex.getMessage());
        }
    }

    private Map<String, Object> chatBody(List<MultipartFile> photos) throws Exception {
        List<Map<String, Object>> userContent = new ArrayList<>();
        userContent.add(Map.of("type", "text", "text", USER));
        for (MultipartFile photo : photos) {
            String mime = photo.getContentType() == null ? "image/jpeg" : photo.getContentType();
            String b64 = Base64.getEncoder().encodeToString(photo.getBytes());
            userContent.add(Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", "data:" + mime + ";base64," + b64)
            ));
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getOpenai().getModel());
        payload.put("temperature", 0);
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM),
                Map.of("role", "user", "content", userContent)
        ));
        return payload;
    }

    VisionResult parse(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").path(0).path("message").path("content").asText("{}");
        JsonNode json = objectMapper.readTree(stripFences(content));

        boolean insufficient = json.path("insufficient").asBoolean(false);
        BigDecimal listPrice = null;
        if (json.path("price_visible").asBoolean(false) && json.hasNonNull("list_price")) {
            listPrice = new BigDecimal(json.get("list_price").asText());
            if (listPrice.compareTo(BigDecimal.ZERO) <= 0) {
                listPrice = null;
            }
        }

        List<String> reasons = new ArrayList<>();
        if (json.path("reasons").isArray()) {
            json.get("reasons").forEach(n -> reasons.add(n.asText()));
        }

        Integer score = json.hasNonNull("bargain_score") ? json.get("bargain_score").asInt() : null;
        String retake = textOrNull(json, "retake_hint");
        boolean tagReadable = json.path("tag_readable").asBoolean(false);
        if (listPrice == null && retake == null) {
            retake = "价签看不太清。再拍一张价签更稳，也可以先跳过。";
        }

        return new VisionResult(
                textOrNull(json, "brand"),
                textOrNull(json, "category"),
                textOrNull(json, "fabric"),
                textOrNull(json, "sku"),
                listPrice,
                json.path("price_visible").asBoolean(false),
                tagReadable,
                json.path("care_label_readable").asBoolean(false),
                insufficient,
                retake,
                score,
                reasons
        );
    }

    private static String textOrNull(JsonNode json, String field) {
        String value = json.path(field).asText(null);
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return value.trim();
    }

    private static String stripFences(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('\n');
            int end = trimmed.lastIndexOf("```");
            if (start > 0 && end > start) {
                return trimmed.substring(start + 1, end).trim();
            }
        }
        return trimmed;
    }

    private static String trimSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
