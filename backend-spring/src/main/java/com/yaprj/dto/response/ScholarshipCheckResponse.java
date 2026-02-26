package com.yaprj.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipCheckResponse {
    private List<ScholarshipMatchResult> results;
    private String checkedAt;
    private CheckSummary summary;
    private Map<String, Object> userConditions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScholarshipMatchResult {
        private ScholarshipInfo scholarship;
        private Boolean isEligible;
        private EligibilityDetail eligibilityDetail;
        private String applyPeriod;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScholarshipInfo {
        private String id;
        private String name;
        private String type;
        private String description;
        private String applyStart;
        private String applyEnd;
        private String externalUrl;
        private Boolean isActive;
        private String organization;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EligibilityDetail {
        private List<String> satisfied;
        private List<String> notSatisfied;
        private List<String> unknown;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckSummary {
        private int eligibleCount;
        private int totalCount;
        private int aiAnalyzedCount;
        private int publicDataCount;
    }
}
