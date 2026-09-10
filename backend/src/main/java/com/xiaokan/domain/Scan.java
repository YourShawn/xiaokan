package com.xiaokan.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scans")
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private String brand;
    private String category;
    private String fabric;
    private String sku;

    @Column(name = "list_price", precision = 12, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "opening_offer", precision = 12, scale = 2)
    private BigDecimal openingOffer;

    @Column(name = "target_min", precision = 12, scale = 2)
    private BigDecimal targetMin;

    @Column(name = "target_max", precision = 12, scale = 2)
    private BigDecimal targetMax;

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "bargain_score")
    private Integer bargainScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reasons_json", nullable = false, columnDefinition = "json")
    private List<String> reasons = new ArrayList<>();

    @Column(name = "needs_retake_tag", nullable = false)
    private boolean needsRetakeTag;

    @Column(name = "retake_hint")
    private String retakeHint;

    @Column(name = "insufficient_evidence", nullable = false)
    private boolean insufficientEvidence;

    @Column(name = "mock_mode", nullable = false)
    private boolean mockMode;

    private Boolean bought;

    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ScanPhoto> photos = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public BigDecimal savedAmount() {
        if (!Boolean.TRUE.equals(bought) || listPrice == null || finalPrice == null) {
            return null;
        }
        return listPrice.subtract(finalPrice);
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFabric() {
        return fabric;
    }

    public void setFabric(String fabric) {
        this.fabric = fabric;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getListPrice() {
        return listPrice;
    }

    public void setListPrice(BigDecimal listPrice) {
        this.listPrice = listPrice;
    }

    public BigDecimal getOpeningOffer() {
        return openingOffer;
    }

    public void setOpeningOffer(BigDecimal openingOffer) {
        this.openingOffer = openingOffer;
    }

    public BigDecimal getTargetMin() {
        return targetMin;
    }

    public void setTargetMin(BigDecimal targetMin) {
        this.targetMin = targetMin;
    }

    public BigDecimal getTargetMax() {
        return targetMax;
    }

    public void setTargetMax(BigDecimal targetMax) {
        this.targetMax = targetMax;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getBargainScore() {
        return bargainScore;
    }

    public void setBargainScore(Integer bargainScore) {
        this.bargainScore = bargainScore;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons == null ? new ArrayList<>() : new ArrayList<>(reasons);
    }

    public boolean isNeedsRetakeTag() {
        return needsRetakeTag;
    }

    public void setNeedsRetakeTag(boolean needsRetakeTag) {
        this.needsRetakeTag = needsRetakeTag;
    }

    public String getRetakeHint() {
        return retakeHint;
    }

    public void setRetakeHint(String retakeHint) {
        this.retakeHint = retakeHint;
    }

    public boolean isInsufficientEvidence() {
        return insufficientEvidence;
    }

    public void setInsufficientEvidence(boolean insufficientEvidence) {
        this.insufficientEvidence = insufficientEvidence;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }

    public Boolean getBought() {
        return bought;
    }

    public void setBought(Boolean bought) {
        this.bought = bought;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<ScanPhoto> getPhotos() {
        return photos;
    }

    public void addPhoto(ScanPhoto photo) {
        photos.add(photo);
        photo.setScan(this);
    }
}
