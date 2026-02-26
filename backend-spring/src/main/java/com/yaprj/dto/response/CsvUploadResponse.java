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
public class CsvUploadResponse {
    private String message;
    private String filename;
    private String uploadedBy;
    private String mode;
    private int totalRows;
    private int success;
    private int failed;
    private int deletedCount;
    private int deactivatedCount;
    private Map<String, Long> previousStats;
    private Map<String, Long> newStats;
    private List<ErrorDetail> errors;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private int row;
        private String error;
        private String name;
    }
}
