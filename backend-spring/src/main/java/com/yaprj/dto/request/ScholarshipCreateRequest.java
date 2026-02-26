package com.yaprj.dto.request;

import com.yaprj.entity.enums.ScholarshipType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScholarshipCreateRequest {
    private String name;
    private String organization;
    private String organizationType;
    private String productType = "장학금";
    private ScholarshipType scholarshipType = ScholarshipType.OTHER;
    private String gpaRequirementText;
    private String incomeRequirementText;
    private String supportDetails;
    private BigDecimal minGpa;
    private Integer maxIncomeLevel;
    private String allowedStatus = "enrolled,expected,leave";
    private String allowedGrades = "1,2,3,4";
    private String websiteUrl;
    private String applyStart;
    private String applyEnd;
    private Boolean isActive = true;
    private Boolean isFeatured = false;
}
