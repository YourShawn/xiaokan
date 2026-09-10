CREATE TABLE scans (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    brand VARCHAR(255) NULL,
    category VARCHAR(255) NULL,
    fabric VARCHAR(512) NULL,
    sku VARCHAR(255) NULL,
    list_price DECIMAL(12, 2) NULL,
    opening_offer DECIMAL(12, 2) NULL,
    target_min DECIMAL(12, 2) NULL,
    target_max DECIMAL(12, 2) NULL,
    max_price DECIMAL(12, 2) NULL,
    bargain_score INT NULL,
    reasons_json JSON NOT NULL,
    needs_retake_tag BOOLEAN NOT NULL DEFAULT FALSE,
    retake_hint VARCHAR(512) NULL,
    insufficient_evidence BOOLEAN NOT NULL DEFAULT FALSE,
    mock_mode BOOLEAN NOT NULL DEFAULT TRUE,
    bought BOOLEAN NULL,
    final_price DECIMAL(12, 2) NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE scan_photos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    scan_id BIGINT NOT NULL,
    sort_order INT NOT NULL,
    original_filename VARCHAR(255) NULL,
    content_type VARCHAR(128) NULL,
    storage_path VARCHAR(512) NOT NULL,
    size_bytes BIGINT NOT NULL,
    CONSTRAINT fk_scan_photos_scan FOREIGN KEY (scan_id) REFERENCES scans (id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE INDEX idx_scans_created_at ON scans (created_at DESC);
CREATE INDEX idx_scan_photos_scan_id ON scan_photos (scan_id);
