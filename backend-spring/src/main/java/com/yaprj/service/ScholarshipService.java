package com.yaprj.service;

import com.yaprj.dto.request.ScholarshipCheckRequest;
import com.yaprj.dto.request.ScholarshipCreateRequest;
import com.yaprj.dto.request.ScholarshipUpdateRequest;
import com.yaprj.dto.response.DashboardStatsResponse;
import com.yaprj.dto.response.ScholarshipCheckResponse;
import com.yaprj.dto.response.ScholarshipCheckResponse.*;
import com.yaprj.dto.response.ScholarshipResponse;
import com.yaprj.entity.Scholarship;
import com.yaprj.entity.enums.ScholarshipType;
import com.yaprj.repository.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScholarshipService {
    
    private final ScholarshipRepository scholarshipRepository;
    
    // ========== 사용자 API ==========
    
    public Map<String, Object> getScholarships(int page, int perPage, String search, 
                                                ScholarshipType type, boolean onlyAccepting) {
        Pageable pageable = PageRequest.of(page - 1, perPage, 
                Sort.by(Sort.Direction.DESC, "isFeatured").and(Sort.by(Sort.Direction.ASC, "applyEnd")));
        
        Page<Scholarship> scholarshipPage;
        
        if (search != null && !search.isEmpty()) {
            scholarshipPage = scholarshipRepository.searchByKeyword(search, pageable);
        } else {
            scholarshipPage = scholarshipRepository.findByIsActiveTrue(pageable);
        }
        
        List<Map<String, Object>> scholarships = scholarshipPage.getContent().stream()
                .map(this::toSimpleMap)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("scholarships", scholarships);
        result.put("total", scholarshipPage.getTotalElements());
        result.put("page", page);
        result.put("per_page", perPage);
        result.put("source", "database");
        
        return result;
    }
    
    public Map<String, Object> getScholarshipDetail(String id) {
        Scholarship s = scholarshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장학금을 찾을 수 없습니다."));
        
        if (!s.getIsActive()) {
            throw new IllegalArgumentException("장학금을 찾을 수 없습니다.");
        }
        
        return toDetailMap(s);
    }
    
    /**
     * 사용자 조건 기반 장학금 자격 확인
     */
    public ScholarshipCheckResponse checkEligibility(ScholarshipCheckRequest request) {
        List<Scholarship> scholarships = scholarshipRepository.findByIsActiveTrueOrderByIsFeaturedDescUpdatedAtDesc();
        
        log.info("자격 확인 시작 - 총 {}개 장학금", scholarships.size());
        log.info("사용자 조건: 학적={}, 학년={}, GPA={}, 소득분위={}", 
                request.getAcademicStatus(), request.getGrade(), request.getGpa(), request.getIncomeLevel());
        
        Map<String, Object> userConditions = new HashMap<>();
        userConditions.put("academic_status", request.getAcademicStatus().getValue());
        userConditions.put("grade", request.getGrade());
        userConditions.put("birth_year", request.getBirthYear());
        userConditions.put("gpa", request.getGpa());
        userConditions.put("income_level", request.getIncomeLevel());
        
        List<ScholarshipMatchResult> results = scholarships.stream()
                .map(s -> checkSingleScholarship(s, request))
                .sorted((a, b) -> {
                    // 정렬: 적격(true) > 확인필요(null) > 부적격(false)
                    if (Boolean.TRUE.equals(a.getIsEligible()) && !Boolean.TRUE.equals(b.getIsEligible())) return -1;
                    if (!Boolean.TRUE.equals(a.getIsEligible()) && Boolean.TRUE.equals(b.getIsEligible())) return 1;
                    if (a.getIsEligible() == null && Boolean.FALSE.equals(b.getIsEligible())) return -1;
                    if (Boolean.FALSE.equals(a.getIsEligible()) && b.getIsEligible() == null) return 1;
                    return 0;
                })
                .collect(Collectors.toList());
        
        int eligibleCount = (int) results.stream().filter(r -> Boolean.TRUE.equals(r.getIsEligible())).count();
        int notEligibleCount = (int) results.stream().filter(r -> Boolean.FALSE.equals(r.getIsEligible())).count();
        int unknownCount = (int) results.stream().filter(r -> r.getIsEligible() == null).count();
        
        log.info("자격 확인 완료 - 적격: {}건, 부적격: {}건, 확인필요: {}건", eligibleCount, notEligibleCount, unknownCount);
        
        return ScholarshipCheckResponse.builder()
                .results(results)
                .checkedAt(LocalDateTime.now().toString())
                .summary(CheckSummary.builder()
                        .eligibleCount(eligibleCount)
                        .totalCount(results.size())
                        .aiAnalyzedCount(0)
                        .publicDataCount(scholarships.size())
                        .build())
                .userConditions(userConditions)
                .build();
    }
    
    /**
     * 개별 장학금 자격 확인
     */
    private ScholarshipMatchResult checkSingleScholarship(Scholarship s, ScholarshipCheckRequest request) {
        List<String> satisfied = new ArrayList<>();
        List<String> notSatisfied = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        
        String userStatus = request.getAcademicStatus().getValue();
        int userGrade = request.getGrade();
        BigDecimal userGpa = request.getGpa();
        int userIncome = request.getIncomeLevel();
        
        Map<String, String> statusKorean = Map.of("enrolled", "재학", "expected", "입학예정", "leave", "휴학");
        
        // 1. 학적상태 체크
        String allowedStatus = s.getAllowedAcademicStatus();
        if (allowedStatus != null && !allowedStatus.isEmpty()) {
            List<String> statusList = Arrays.asList(allowedStatus.split(","));
            if (statusList.stream().anyMatch(st -> st.trim().equalsIgnoreCase(userStatus))) {
                satisfied.add("학적상태 충족 (" + statusKorean.getOrDefault(userStatus, userStatus) + ")");
            } else {
                String allowed = statusList.stream()
                        .map(st -> statusKorean.getOrDefault(st.trim(), st.trim()))
                        .collect(Collectors.joining("/"));
                notSatisfied.add("학적상태 미충족 (요구: " + allowed + ")");
            }
        } else {
            // 원본 텍스트에서 힌트 찾기
            String hint = combineTexts(s.getUniversityCategory(), s.getSpecialQualification());
            if (!hint.isEmpty() && containsAny(hint, "재학", "신입", "휴학", "입학")) {
                unknown.add("학적상태 직접 확인 필요");
            }
        }
        
        // 2. 학년 체크
        String allowedGrades = s.getAllowedGrades();
        if (allowedGrades != null && !allowedGrades.isEmpty()) {
            List<Integer> gradeList = Arrays.stream(allowedGrades.split(","))
                    .map(String::trim)
                    .filter(g -> g.matches("\\d+"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            
            if (!gradeList.isEmpty()) {
                if (gradeList.contains(userGrade)) {
                    satisfied.add("학년 충족 (" + userGrade + "학년)");
                } else {
                    String allowed = gradeList.stream().map(g -> g + "학년").collect(Collectors.joining("/"));
                    notSatisfied.add("학년 미충족 (요구: " + allowed + ")");
                }
            }
        } else if (s.getGradeSemester() != null && !s.getGradeSemester().isEmpty()) {
            // 파싱 안됐지만 원본 데이터 있음
            if (!containsAny(s.getGradeSemester(), "전학년", "제한없음", "무관")) {
                unknown.add("학년 조건 직접 확인: " + truncate(s.getGradeSemester(), 20));
            }
        }
        
        // 3. 성적(GPA) 체크
        BigDecimal minGpa = s.getMinGpa();
        if (minGpa != null) {
            if (userGpa.compareTo(minGpa) >= 0) {
                satisfied.add(String.format("성적 충족 (%.1f ≥ %.1f)", userGpa, minGpa));
            } else {
                notSatisfied.add(String.format("성적 미충족 (%.1f < %.1f 이상 필요)", userGpa, minGpa));
            }
        } else if (s.getGradeCriteria() != null && !s.getGradeCriteria().isEmpty()) {
            // 파싱 안됐지만 원본 데이터 있음
            if (containsAny(s.getGradeCriteria(), "제한없음", "무관", "해당없음")) {
                satisfied.add("성적 제한 없음");
            } else {
                unknown.add("성적 조건 직접 확인: " + truncate(s.getGradeCriteria(), 25));
            }
        }
        
        // 4. 소득분위 체크
        Integer maxIncome = s.getMaxIncomeLevel();
        if (maxIncome != null) {
            if (userIncome <= maxIncome) {
                satisfied.add(String.format("소득분위 충족 (%d분위 ≤ %d분위 이하)", userIncome, maxIncome));
            } else {
                notSatisfied.add(String.format("소득분위 미충족 (%d분위 > %d분위 이하 필요)", userIncome, maxIncome));
            }
        } else if (s.getIncomeCriteria() != null && !s.getIncomeCriteria().isEmpty()) {
            // 파싱 안됐지만 원본 데이터 있음
            if (containsAny(s.getIncomeCriteria(), "제한없음", "무관", "해당없음", "소득무관")) {
                satisfied.add("소득 제한 없음");
            } else {
                unknown.add("소득 조건 직접 확인: " + truncate(s.getIncomeCriteria(), 25));
            }
        }
        
        // 5. 지역 체크
        String regionLimit = s.getRegionLimit();
        if (regionLimit != null && !regionLimit.isEmpty()) {
            unknown.add("지역 제한 확인 필요: " + regionLimit);
        } else if (s.getResidencyDetail() != null && !s.getResidencyDetail().isEmpty()) {
            if (!containsAny(s.getResidencyDetail(), "전국", "제한없음", "무관")) {
                unknown.add("지역 조건 직접 확인 필요");
            }
        }
        
        // 6. 특정자격 체크
        if (s.getSpecialQualification() != null && !s.getSpecialQualification().isEmpty()) {
            if (!containsAny(s.getSpecialQualification(), "제한없음", "무관", "해당없음")) {
                unknown.add("특정자격 확인 필요: " + truncate(s.getSpecialQualification(), 30));
            }
        }
        
        // 적격 여부 판정
        Boolean isEligible;
        if (!notSatisfied.isEmpty()) {
            // 하나라도 미충족이면 부적격
            isEligible = false;
        } else if (satisfied.size() >= 1) {
            // 미충족 없고 최소 1개 충족이면 적격
            isEligible = true;
        } else {
            // 판단할 조건이 없음 → 확인 필요
            isEligible = null;
            if (unknown.isEmpty()) {
                unknown.add("상세 조건 직접 확인 필요");
            }
        }
        
        // 신청 기간
        String applyPeriod = null;
        if (s.getApplyStart() != null || s.getApplyEnd() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd");
            String start = s.getApplyStart() != null ? s.getApplyStart().format(formatter) : "?";
            String end = s.getApplyEnd() != null ? s.getApplyEnd().format(formatter) : "?";
            applyPeriod = start + " ~ " + end;
        }
        
        return ScholarshipMatchResult.builder()
                .scholarship(ScholarshipInfo.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .type(s.getScholarshipType() != null ? s.getScholarshipType().getValue() : "other")
                        .description(s.getSupportDetails())
                        .applyStart(s.getApplyStart() != null ? s.getApplyStart().toString() : null)
                        .applyEnd(s.getApplyEnd() != null ? s.getApplyEnd().toString() : null)
                        .externalUrl(s.getWebsiteUrl())
                        .isActive(s.getIsActive())
                        .organization(s.getOrganization())
                        .build())
                .isEligible(isEligible)
                .eligibilityDetail(EligibilityDetail.builder()
                        .satisfied(satisfied)
                        .notSatisfied(notSatisfied)
                        .unknown(unknown)
                        .build())
                .applyPeriod(applyPeriod)
                .build();
    }
    
    // ========== Helper Methods ==========
    
    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
    
    private String combineTexts(String... texts) {
        StringBuilder sb = new StringBuilder();
        for (String text : texts) {
            if (text != null && !text.isEmpty()) {
                sb.append(text).append(" ");
            }
        }
        return sb.toString().trim();
    }
    
    private boolean containsAny(String text, String... keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }
    
    private Map<String, Object> toSimpleMap(Scholarship s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("name", s.getName());
        map.put("organization", s.getOrganization());
        map.put("type", s.getScholarshipType() != null ? s.getScholarshipType().getValue() : "other");
        map.put("description", s.getSupportDetails());
        map.put("apply_start", s.getApplyStart() != null ? s.getApplyStart().toString() : null);
        map.put("apply_end", s.getApplyEnd() != null ? s.getApplyEnd().toString() : null);
        map.put("website_url", s.getWebsiteUrl());
        map.put("is_featured", s.getIsFeatured());
        return map;
    }
    
    private Map<String, Object> toDetailMap(Scholarship s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("name", s.getName());
        map.put("organization", s.getOrganization());
        map.put("organization_type", s.getOrganizationType());
        map.put("product_type", s.getProductType());
        map.put("financial_aid_type", s.getFinancialAidType());
        map.put("type", s.getScholarshipType() != null ? s.getScholarshipType().getValue() : "other");
        map.put("university_category", s.getUniversityCategory());
        map.put("grade_semester", s.getGradeSemester());
        map.put("major_category", s.getMajorCategory());
        map.put("grade_criteria", s.getGradeCriteria());
        map.put("income_criteria", s.getIncomeCriteria());
        map.put("support_details", s.getSupportDetails());
        map.put("special_qualification", s.getSpecialQualification());
        map.put("residency_detail", s.getResidencyDetail());
        map.put("selection_method", s.getSelectionMethod());
        map.put("selection_count", s.getSelectionCount());
        map.put("eligibility_restriction", s.getEligibilityRestriction());
        map.put("recommendation_required", s.getRecommendationRequired());
        map.put("required_documents", s.getRequiredDocuments());
        map.put("website_url", s.getWebsiteUrl());
        map.put("apply_start", s.getApplyStart() != null ? s.getApplyStart().toString() : null);
        map.put("apply_end", s.getApplyEnd() != null ? s.getApplyEnd().toString() : null);
        return map;
    }
    
    // ========== Featured & Accepting ==========
    
    public List<Map<String, Object>> getFeaturedScholarships() {
        return scholarshipRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByUpdatedAtDesc().stream()
                .map(this::toSimpleMap)
                .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getAcceptingScholarships() {
        LocalDate today = LocalDate.now();
        return scholarshipRepository.findAcceptingApplications(today).stream()
                .map(this::toSimpleMap)
                .collect(Collectors.toList());
    }
    
    // ========== Admin API ==========
    
    public DashboardStatsResponse getDashboardStats() {
        long total = scholarshipRepository.count();
        long active = scholarshipRepository.countByIsActiveTrue();
        long inactive = scholarshipRepository.countByIsActiveFalse();
        long featured = scholarshipRepository.countByIsFeaturedTrue();
        long accepting = scholarshipRepository.findAcceptingApplications(LocalDate.now()).size();
        
        Map<String, Long> byType = new HashMap<>();
        try {
            byType = scholarshipRepository.countByScholarshipType().stream()
                    .collect(Collectors.toMap(
                            row -> row[0] != null ? ((ScholarshipType) row[0]).getValue() : "other",
                            row -> (Long) row[1]
                    ));
        } catch (Exception e) {
            log.warn("타입별 통계 조회 실패", e);
        }
        
        Map<String, Long> byOrgType = new HashMap<>();
        try {
            byOrgType = scholarshipRepository.countByOrganizationType().stream()
                    .collect(Collectors.toMap(
                            row -> row[0] != null ? (String) row[0] : "기타",
                            row -> (Long) row[1]
                    ));
        } catch (Exception e) {
            log.warn("기관유형별 통계 조회 실패", e);
        }
        
        return DashboardStatsResponse.builder()
                .totalScholarships(total)
                .activeScholarships(active)
                .inactiveScholarships(inactive)
                .featuredScholarships(featured)
                .acceptingApplications(accepting)
                .recentUpdates(0)
                .byType(byType)
                .byOrganizationType(byOrgType)
                .build();
    }
    
    public Map<String, Object> getAdminScholarships(int page, int perPage, String search,
                                                     ScholarshipType type, Boolean isActive, Boolean isFeatured) {
        Pageable pageable = PageRequest.of(page - 1, perPage,
                Sort.by(Sort.Direction.DESC, "isActive").and(Sort.by(Sort.Direction.DESC, "updatedAt")));
        
        Page<Scholarship> scholarshipPage = scholarshipRepository.findWithFilters(
                search, type, isActive, isFeatured, pageable);
        
        List<ScholarshipResponse> scholarships = scholarshipPage.getContent().stream()
                .map(ScholarshipResponse::from)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("scholarships", scholarships);
        result.put("total", scholarshipPage.getTotalElements());
        result.put("page", page);
        result.put("per_page", perPage);
        result.put("total_pages", scholarshipPage.getTotalPages());
        
        return result;
    }
    
    public ScholarshipResponse getAdminScholarshipDetail(String id) {
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장학금을 찾을 수 없습니다."));
        return ScholarshipResponse.from(scholarship);
    }
    
    @Transactional
    public ScholarshipResponse createScholarship(ScholarshipCreateRequest request) {
        Scholarship scholarship = Scholarship.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .organization(request.getOrganization())
                .organizationType(request.getOrganizationType())
                .productType(request.getProductType())
                .scholarshipType(request.getScholarshipType())
                .gradeCriteria(request.getGpaRequirementText())
                .incomeCriteria(request.getIncomeRequirementText())
                .supportDetails(request.getSupportDetails())
                .minGpa(request.getMinGpa())
                .maxIncomeLevel(request.getMaxIncomeLevel())
                .allowedAcademicStatus(request.getAllowedStatus())
                .allowedGrades(request.getAllowedGrades())
                .websiteUrl(request.getWebsiteUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .build();
        
        if (request.getApplyStart() != null) {
            scholarship.setApplyStart(LocalDate.parse(request.getApplyStart()));
        }
        if (request.getApplyEnd() != null) {
            scholarship.setApplyEnd(LocalDate.parse(request.getApplyEnd()));
        }
        
        return ScholarshipResponse.from(scholarshipRepository.save(scholarship));
    }
    
    @Transactional
    public ScholarshipResponse updateScholarship(String id, ScholarshipUpdateRequest request) {
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장학금을 찾을 수 없습니다."));
        
        if (request.getName() != null) scholarship.setName(request.getName());
        if (request.getOrganization() != null) scholarship.setOrganization(request.getOrganization());
        if (request.getMinGpa() != null) scholarship.setMinGpa(request.getMinGpa());
        if (request.getMaxIncomeLevel() != null) scholarship.setMaxIncomeLevel(request.getMaxIncomeLevel());
        if (request.getAllowedStatus() != null) scholarship.setAllowedAcademicStatus(request.getAllowedStatus());
        if (request.getAllowedGrades() != null) scholarship.setAllowedGrades(request.getAllowedGrades());
        if (request.getRegionRestriction() != null) scholarship.setRegionLimit(request.getRegionRestriction());
        if (request.getIsActive() != null) scholarship.setIsActive(request.getIsActive());
        if (request.getIsFeatured() != null) scholarship.setIsFeatured(request.getIsFeatured());
        if (request.getWebsiteUrl() != null) scholarship.setWebsiteUrl(request.getWebsiteUrl());
        
        return ScholarshipResponse.from(scholarshipRepository.save(scholarship));
    }
    
    @Transactional
    public void deleteScholarship(String id) {
        if (!scholarshipRepository.existsById(id)) {
            throw new IllegalArgumentException("장학금을 찾을 수 없습니다.");
        }
        scholarshipRepository.deleteById(id);
    }
    
    @Transactional
    public int deleteAllScholarships() {
        int count = (int) scholarshipRepository.count();
        scholarshipRepository.deleteAll();
        return count;
    }
    
    @Transactional
    public int deactivateAllScholarships() {
        return scholarshipRepository.deactivateAll();
    }
    
    @Transactional
    public int deleteInactiveScholarships() {
        return scholarshipRepository.deleteInactive();
    }
    
    @Transactional
    public void bulkUpdateScholarships(List<String> ids, Boolean isActive, Boolean isFeatured) {
        List<Scholarship> scholarships = scholarshipRepository.findAllById(ids);
        
        for (Scholarship s : scholarships) {
            if (isActive != null) s.setIsActive(isActive);
            if (isFeatured != null) s.setIsFeatured(isFeatured);
        }
        
        scholarshipRepository.saveAll(scholarships);
    }
}
