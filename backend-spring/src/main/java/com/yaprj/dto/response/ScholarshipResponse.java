package com.yaprj.dto.response;

import com.yaprj.entity.Scholarship;
import com.yaprj.entity.enums.ScholarshipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipResponse {
    private String id;
    private String name;
    private String organization;
    private String organizationType;
    private String productType;
    private String financialAidType;
    private String scholarshipType;
    
    // CSV 원본 데이터
    private String universityCategory;
    private String gradeSemester;
    private String majorCategory;
    private String gradeCriteria;
    private String incomeCriteria;
    private String supportDetails;
    private String specialQualification;
    private String residencyDetail;
    private String selectionMethod;
    private String selectionCount;
    private String eligibilityRestriction;
    private String recommendationRequired;
    private String requiredDocuments;
    private String websiteUrl;
    private String applyStart;
    private String applyEnd;
    
    // 파싱된 정량 데이터
    private BigDecimal minGpa;
    private Integer maxIncomeLevel;
    private String allowedAcademicStatus;
    private String allowedGrades;
    private String allowedUniversityTypes;
    private String regionLimit;
    
    // 관리
    private Boolean isActive;
    private Boolean isFeatured;
    private String createdAt;
    private String updatedAt;
    
    public static ScholarshipResponse from(Scholarship s) {
        return ScholarshipResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .organization(s.getOrganization())
                .organizationType(s.getOrganizationType())
                .productType(s.getProductType())
                .financialAidType(s.getFinancialAidType())
                .scholarshipType(s.getScholarshipType() != null ? s.getScholarshipType().getValue() : "other")
                .universityCategory(s.getUniversityCategory())
                .gradeSemester(s.getGradeSemester())
                .majorCategory(s.getMajorCategory())
                .gradeCriteria(s.getGradeCriteria())
                .incomeCriteria(s.getIncomeCriteria())
                .supportDetails(s.getSupportDetails())
                .specialQualification(s.getSpecialQualification())
                .residencyDetail(s.getResidencyDetail())
                .selectionMethod(s.getSelectionMethod())
                .selectionCount(s.getSelectionCount())
                .eligibilityRestriction(s.getEligibilityRestriction())
                .recommendationRequired(s.getRecommendationRequired())
                .requiredDocuments(s.getRequiredDocuments())
                .websiteUrl(s.getWebsiteUrl())
                .applyStart(s.getApplyStart() != null ? s.getApplyStart().toString() : null)
                .applyEnd(s.getApplyEnd() != null ? s.getApplyEnd().toString() : null)
                .minGpa(s.getMinGpa())
                .maxIncomeLevel(s.getMaxIncomeLevel())
                .allowedAcademicStatus(s.getAllowedAcademicStatus())
                .allowedGrades(s.getAllowedGrades())
                .allowedUniversityTypes(s.getAllowedUniversityTypes())
                .regionLimit(s.getRegionLimit())
                .isActive(s.getIsActive())
                .isFeatured(s.getIsFeatured())
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .updatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
                .build();
    }
}
