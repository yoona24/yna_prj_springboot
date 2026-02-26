package com.yaprj.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalScholarships;
    private long activeScholarships;
    private long inactiveScholarships;
    private long featuredScholarships;
    private long acceptingApplications;
    private long recentUpdates;
    private Map<String, Long> byType;
    private Map<String, Long> byOrganizationType;
}
