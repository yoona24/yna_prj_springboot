package com.yaprj.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScholarshipUpdateRequest {
    private String name;
    private String organization;
    private BigDecimal minGpa;
    private Integer maxIncomeLevel;
    private String allowedStatus;
    private String allowedGrades;
    private String regionRestriction;
    private Boolean isActive;
    private Boolean isFeatured;
    private String websiteUrl;
}
