package com.yaprj.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.yaprj.dto.response.CsvUploadResponse;
import com.yaprj.entity.Scholarship;
import com.yaprj.entity.enums.ScholarshipType;
import com.yaprj.repository.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvParserService {
    
    private final ScholarshipRepository scholarshipRepository;
    
    // 날짜 포맷들
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"),
            DateTimeFormatter.ofPattern("yyyy년MM월dd일")
    );
    
    // CSV 헤더 → 인덱스 매핑
    private static final String[] EXPECTED_HEADERS = {
            "번호", "운영기관명", "상품명", "운영기관구분", "상품구분", 
            "학자금유형구분", "대학구분", "학년구분", "학과구분",
            "성적기준 상세내용", "소득기준 상세내용", "지원내역 상세내용",
            "특정자격 상세내용", "지역거주여부 상세내용", "선발방법 상세내용",
            "선발인원 상세내용", "자격제한 상세내용", "추천필요여부 상세내용",
            "제출서류 상세내용", "홈페이지 주소", "모집시작일", "모집종료일"
    };
    
    @Transactional
    public CsvUploadResponse parseAndSave(MultipartFile file, String mode, String uploadedBy) {
        List<CsvUploadResponse.ErrorDetail> errors = new ArrayList<>();
        int success = 0;
        int failed = 0;
        int deletedCount = 0;
        int deactivatedCount = 0;
        
        Map<String, Long> previousStats = new HashMap<>();
        previousStats.put("total", scholarshipRepository.count());
        previousStats.put("active", scholarshipRepository.countByIsActiveTrue());
        
        // 모드에 따른 기존 데이터 처리
        if ("replace".equalsIgnoreCase(mode)) {
            deletedCount = (int) scholarshipRepository.count();
            scholarshipRepository.deleteAll();
            log.info("기존 데이터 {}건 삭제", deletedCount);
        } else if ("deactivate".equalsIgnoreCase(mode)) {
            deactivatedCount = scholarshipRepository.deactivateAll();
            log.info("기존 데이터 {}건 비활성화", deactivatedCount);
        }
        
        try {
            // 인코딩 감지
            Charset charset = detectCharset(file);
            log.info("감지된 인코딩: {}", charset);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), charset));
            CSVReader csvReader = new CSVReader(reader);
            
            List<String[]> allRows = csvReader.readAll();
            if (allRows.isEmpty()) {
                throw new IllegalArgumentException("CSV 파일이 비어있습니다.");
            }
            
            // 헤더 파싱
            String[] headers = allRows.get(0);
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);
            log.info("CSV 헤더: {}", Arrays.toString(headers));
            log.info("헤더 인덱스 매핑: {}", headerIndex);
            
            // 데이터 행 처리
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                int rowNum = i + 1;
                
                try {
                    Scholarship scholarship = parseRow(row, headerIndex, rowNum);
                    if (scholarship != null) {
                        scholarshipRepository.save(scholarship);
                        success++;
                    }
                } catch (Exception e) {
                    failed++;
                    String name = getCell(row, headerIndex, "상품명");
                    log.warn("Row {} 파싱 실패: {} - {}", rowNum, name, e.getMessage());
                    errors.add(CsvUploadResponse.ErrorDetail.builder()
                            .row(rowNum)
                            .error(e.getMessage())
                            .name(name != null ? name : "Unknown")
                            .build());
                    if (errors.size() >= 30) {
                        log.warn("에러가 30건 이상이므로 중단");
                        break;
                    }
                }
            }
            
            csvReader.close();
            
        } catch (IOException | CsvException e) {
            log.error("CSV 파싱 오류", e);
            throw new RuntimeException("CSV 파일 처리 중 오류 발생: " + e.getMessage());
        }
        
        Map<String, Long> newStats = new HashMap<>();
        newStats.put("total", scholarshipRepository.count());
        newStats.put("active", scholarshipRepository.countByIsActiveTrue());
        
        log.info("CSV 업로드 완료: 성공 {}건, 실패 {}건", success, failed);
        
        return CsvUploadResponse.builder()
                .message("CSV 업로드 완료")
                .filename(file.getOriginalFilename())
                .uploadedBy(uploadedBy)
                .mode(mode)
                .totalRows(success + failed)
                .success(success)
                .failed(failed)
                .deletedCount(deletedCount)
                .deactivatedCount(deactivatedCount)
                .previousStats(previousStats)
                .newStats(newStats)
                .errors(errors)
                .build();
    }
    
    /**
     * 인코딩 자동 감지
     */
    private Charset detectCharset(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        
        // BOM 체크 (UTF-8)
        if (bytes.length >= 3 && bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
            return Charset.forName("UTF-8");
        }
        
        // CP949 (한글 Windows) 체크
        try {
            String content = new String(bytes, Charset.forName("CP949"));
            if (content.contains("운영기관명") || content.contains("상품명") || content.contains("장학")) {
                return Charset.forName("CP949");
            }
        } catch (Exception ignored) {}
        
        // EUC-KR 체크
        try {
            String content = new String(bytes, Charset.forName("EUC-KR"));
            if (content.contains("운영기관명") || content.contains("상품명") || content.contains("장학")) {
                return Charset.forName("EUC-KR");
            }
        } catch (Exception ignored) {}
        
        return Charset.forName("UTF-8");
    }
    
    /**
     * 헤더 이름 → 인덱스 매핑 생성
     */
    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim()
                    .replaceAll("[\\uFEFF\\u200B]", "")  // BOM 제거
                    .replaceAll("\\s+", " ");  // 다중 공백 정리
            index.put(header, i);
        }
        return index;
    }
    
    /**
     * 셀 값 가져오기
     */
    private String getCell(String[] row, Map<String, Integer> headerIndex, String... headerNames) {
        for (String name : headerNames) {
            Integer idx = headerIndex.get(name);
            if (idx != null && idx < row.length) {
                String value = row[idx].trim();
                if (!value.isEmpty() && !value.equals("-") && !value.equals("해당없음") && !value.equals("없음")) {
                    return value;
                }
            }
        }
        return null;
    }
    
    /**
     * CSV 행 → Scholarship 엔티티 변환
     */
    private Scholarship parseRow(String[] row, Map<String, Integer> headerIndex, int rowNum) {
        // 필수 필드 확인
        String name = getCell(row, headerIndex, "상품명");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("상품명이 없습니다.");
        }
        
        String organization = getCell(row, headerIndex, "운영기관명");
        if (organization == null || organization.isEmpty()) {
            organization = "미상";
        }
        
        // CSV 원본 데이터 저장
        String rowNumberStr = getCell(row, headerIndex, "번호");
        Integer csvRowNumber = null;
        if (rowNumberStr != null) {
            try {
                csvRowNumber = Integer.parseInt(rowNumberStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {
                csvRowNumber = rowNum;
            }
        } else {
            csvRowNumber = rowNum;
        }
        
        String organizationType = getCell(row, headerIndex, "운영기관구분");
        String productType = getCell(row, headerIndex, "상품구분");
        String financialAidType = getCell(row, headerIndex, "학자금유형구분");
        String universityCategory = getCell(row, headerIndex, "대학구분");
        String gradeSemester = getCell(row, headerIndex, "학년구분");
        String majorCategory = getCell(row, headerIndex, "학과구분");
        String gradeCriteria = getCell(row, headerIndex, "성적기준 상세내용");
        String incomeCriteria = getCell(row, headerIndex, "소득기준 상세내용");
        String supportDetails = getCell(row, headerIndex, "지원내역 상세내용");
        String specialQualification = getCell(row, headerIndex, "특정자격 상세내용");
        String residencyDetail = getCell(row, headerIndex, "지역거주여부 상세내용");
        String selectionMethod = getCell(row, headerIndex, "선발방법 상세내용");
        String selectionCount = getCell(row, headerIndex, "선발인원 상세내용");
        String eligibilityRestriction = getCell(row, headerIndex, "자격제한 상세내용");
        String recommendationRequired = getCell(row, headerIndex, "추천필요여부 상세내용");
        String requiredDocuments = getCell(row, headerIndex, "제출서류 상세내용");
        String websiteUrl = getCell(row, headerIndex, "홈페이지 주소");
        
        String applyStartStr = getCell(row, headerIndex, "모집시작일");
        String applyEndStr = getCell(row, headerIndex, "모집종료일");
        LocalDate applyStart = parseDate(applyStartStr);
        LocalDate applyEnd = parseDate(applyEndStr);
        
        // 정량적 데이터 파싱
        BigDecimal minGpa = extractMinGpa(gradeCriteria);
        Integer maxIncomeLevel = extractMaxIncomeLevel(incomeCriteria);
        String allowedAcademicStatus = extractAcademicStatus(universityCategory, specialQualification, eligibilityRestriction);
        String allowedGrades = extractGrades(gradeSemester, eligibilityRestriction);
        String allowedUniversityTypes = extractUniversityTypes(universityCategory);
        String regionLimit = extractRegionLimit(residencyDetail);
        ScholarshipType scholarshipType = detectScholarshipType(name, financialAidType, organization, productType);
        
        return Scholarship.builder()
                .id(UUID.randomUUID().toString())
                // CSV 원본 데이터
                .csvRowNumber(csvRowNumber)
                .organization(organization)
                .name(name)
                .organizationType(organizationType)
                .productType(productType)
                .financialAidType(financialAidType)
                .universityCategory(universityCategory)
                .gradeSemester(gradeSemester)
                .majorCategory(majorCategory)
                .gradeCriteria(gradeCriteria)
                .incomeCriteria(incomeCriteria)
                .supportDetails(supportDetails)
                .specialQualification(specialQualification)
                .residencyDetail(residencyDetail)
                .selectionMethod(selectionMethod)
                .selectionCount(selectionCount)
                .eligibilityRestriction(eligibilityRestriction)
                .recommendationRequired(recommendationRequired)
                .requiredDocuments(requiredDocuments)
                .websiteUrl(websiteUrl)
                .applyStart(applyStart)
                .applyEnd(applyEnd)
                // 파싱된 정량 데이터
                .scholarshipType(scholarshipType)
                .minGpa(minGpa)
                .maxIncomeLevel(maxIncomeLevel)
                .allowedAcademicStatus(allowedAcademicStatus)
                .allowedGrades(allowedGrades)
                .allowedUniversityTypes(allowedUniversityTypes)
                .regionLimit(regionLimit)
                // 관리용
                .isActive(true)
                .isFeatured(false)
                .build();
    }
    
    /**
     * 날짜 파싱
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        
        // 숫자만 추출 (8자리)
        String digitsOnly = dateStr.replaceAll("[^0-9]", "");
        if (digitsOnly.length() == 8) {
            try {
                return LocalDate.parse(digitsOnly, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (DateTimeParseException ignored) {}
        }
        
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException ignored) {}
        }
        
        return null;
    }
    
    /**
     * 성적 기준에서 최소 GPA 추출 (4.5 만점 기준)
     */
    private BigDecimal extractMinGpa(String text) {
        if (text == null || text.isEmpty()) return null;
        
        // "제한없음", "무관" 등은 null
        if (containsAny(text, "제한없음", "제한 없음", "무관", "해당없음", "해당 없음")) {
            return null;
        }
        
        // 패턴 1: "3.0/4.5", "3.0점/4.5점", "3.0 이상"
        Pattern pattern1 = Pattern.compile("([0-9](?:\\.[0-9]{1,2})?)\\s*(?:점)?\\s*/\\s*4\\.5");
        Matcher m1 = pattern1.matcher(text);
        if (m1.find()) {
            return new BigDecimal(m1.group(1));
        }
        
        // 패턴 2: "평점 3.0 이상", "성적 3.0 이상", "학점 3.0 이상"
        Pattern pattern2 = Pattern.compile("(?:평점|성적|학점|GPA)\\s*([0-9](?:\\.[0-9]{1,2})?)\\s*(?:점)?\\s*이상");
        Matcher m2 = pattern2.matcher(text);
        if (m2.find()) {
            return new BigDecimal(m2.group(1));
        }
        
        // 패턴 3: "3.0 이상", "3.0점 이상"
        Pattern pattern3 = Pattern.compile("([0-9]\\.[0-9]{1,2})\\s*(?:점)?\\s*이상");
        Matcher m3 = pattern3.matcher(text);
        if (m3.find()) {
            BigDecimal value = new BigDecimal(m3.group(1));
            if (value.compareTo(new BigDecimal("5")) <= 0) {
                return value;
            }
        }
        
        // 패턴 4: "B학점 이상", "B+ 이상" 등 학점 등급
        Pattern pattern4 = Pattern.compile("([ABCD][+\\-]?)\\s*(?:학점)?\\s*이상");
        Matcher m4 = pattern4.matcher(text.toUpperCase());
        if (m4.find()) {
            return convertLetterGradeToGpa(m4.group(1));
        }
        
        // 패턴 5: 백분위 "80점 이상", "80% 이상" → 4.5로 환산
        Pattern pattern5 = Pattern.compile("([0-9]{2,3})\\s*(?:점|%)\\s*이상");
        Matcher m5 = pattern5.matcher(text);
        if (m5.find()) {
            int percent = Integer.parseInt(m5.group(1));
            if (percent >= 60 && percent <= 100) {
                // 60점 = 2.0, 100점 = 4.5 선형 환산
                BigDecimal gpa = new BigDecimal(percent - 60)
                        .multiply(new BigDecimal("2.5"))
                        .divide(new BigDecimal("40"), 2, RoundingMode.HALF_UP)
                        .add(new BigDecimal("2.0"));
                return gpa;
            }
        }
        
        return null;
    }
    
    /**
     * 학점 등급을 GPA로 변환
     */
    private BigDecimal convertLetterGradeToGpa(String grade) {
        Map<String, BigDecimal> gradeMap = new HashMap<>();
        gradeMap.put("A+", new BigDecimal("4.5"));
        gradeMap.put("A", new BigDecimal("4.0"));
        gradeMap.put("A-", new BigDecimal("3.7"));
        gradeMap.put("B+", new BigDecimal("3.3"));
        gradeMap.put("B", new BigDecimal("3.0"));
        gradeMap.put("B-", new BigDecimal("2.7"));
        gradeMap.put("C+", new BigDecimal("2.3"));
        gradeMap.put("C", new BigDecimal("2.0"));
        gradeMap.put("C-", new BigDecimal("1.7"));
        gradeMap.put("D+", new BigDecimal("1.3"));
        gradeMap.put("D", new BigDecimal("1.0"));
        return gradeMap.get(grade.toUpperCase());
    }
    
    /**
     * 소득 기준에서 최대 소득분위 추출
     */
    private Integer extractMaxIncomeLevel(String text) {
        if (text == null || text.isEmpty()) return null;
        
        // "제한없음", "무관" 등은 null (제한 없음)
        if (containsAny(text, "제한없음", "제한 없음", "무관", "해당없음", "해당 없음", "소득무관")) {
            return null;
        }
        
        // "기초생활수급자", "차상위계층" → 2분위
        if (containsAny(text, "기초생활", "차상위")) {
            return 2;
        }
        
        // "8분위 이하", "소득 8분위 이하"
        Pattern pattern1 = Pattern.compile("([0-9]{1,2})\\s*분위\\s*이하");
        Matcher m1 = pattern1.matcher(text);
        if (m1.find()) {
            int level = Integer.parseInt(m1.group(1));
            if (level >= 1 && level <= 10) return level;
        }
        
        // "1~8분위", "1-8분위"
        Pattern pattern2 = Pattern.compile("[1-9]\\s*[~\\-]\\s*([0-9]{1,2})\\s*분위");
        Matcher m2 = pattern2.matcher(text);
        if (m2.find()) {
            int level = Integer.parseInt(m2.group(1));
            if (level >= 1 && level <= 10) return level;
        }
        
        // "8구간 이하"
        Pattern pattern3 = Pattern.compile("([0-9]{1,2})\\s*구간\\s*이하");
        Matcher m3 = pattern3.matcher(text);
        if (m3.find()) {
            int level = Integer.parseInt(m3.group(1));
            if (level >= 1 && level <= 10) return level;
        }
        
        // 단순히 "8분위"만 언급된 경우
        Pattern pattern4 = Pattern.compile("([0-9])\\s*분위");
        Matcher m4 = pattern4.matcher(text);
        if (m4.find()) {
            int level = Integer.parseInt(m4.group(1));
            if (level >= 1 && level <= 10) return level;
        }
        
        return null;
    }
    
    /**
     * 학적 상태 추출 (재학, 입학예정, 휴학)
     */
    private String extractAcademicStatus(String universityCategory, String specialQualification, String eligibilityRestriction) {
        String combined = combineTexts(universityCategory, specialQualification, eligibilityRestriction);
        if (combined.isEmpty()) return null;
        
        Set<String> statuses = new LinkedHashSet<>();
        
        if (containsAny(combined, "재학", "재학생", "재학 중")) {
            statuses.add("enrolled");
        }
        if (containsAny(combined, "신입생", "입학예정", "입학 예정", "예비", "합격자", "신입")) {
            statuses.add("expected");
        }
        if (containsAny(combined, "휴학", "휴학생")) {
            statuses.add("leave");
        }
        
        return statuses.isEmpty() ? null : String.join(",", statuses);
    }
    
    /**
     * 허용 학년 추출
     */
    private String extractGrades(String gradeSemester, String eligibilityRestriction) {
        String combined = combineTexts(gradeSemester, eligibilityRestriction);
        if (combined.isEmpty()) return null;
        
        // "전학년", "제한없음" → null (제한 없음)
        if (containsAny(combined, "전학년", "전 학년", "제한없음", "제한 없음", "무관")) {
            return null;
        }
        
        Set<Integer> grades = new TreeSet<>();
        
        // "1~4학년", "1-4학년" 범위
        Pattern rangePattern = Pattern.compile("([1-6])\\s*[~\\-]\\s*([1-6])\\s*학년");
        Matcher rangeMatcher = rangePattern.matcher(combined);
        if (rangeMatcher.find()) {
            int start = Integer.parseInt(rangeMatcher.group(1));
            int end = Integer.parseInt(rangeMatcher.group(2));
            for (int i = start; i <= end; i++) {
                grades.add(i);
            }
        }
        
        // 개별 "1학년", "2학년"
        Pattern singlePattern = Pattern.compile("([1-6])\\s*학년");
        Matcher singleMatcher = singlePattern.matcher(combined);
        while (singleMatcher.find()) {
            grades.add(Integer.parseInt(singleMatcher.group(1)));
        }
        
        // "신입생" → 1학년
        if (containsAny(combined, "신입생", "신입", "입학예정")) {
            grades.add(1);
        }
        
        return grades.isEmpty() ? null : grades.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
    
    /**
     * 대학 유형 추출
     */
    private String extractUniversityTypes(String universityCategory) {
        if (universityCategory == null || universityCategory.isEmpty()) return null;
        
        Set<String> types = new LinkedHashSet<>();
        
        if (containsAny(universityCategory, "4년제", "4년", "대학교")) {
            types.add("4년제");
        }
        if (containsAny(universityCategory, "전문대", "2년제", "2,3년제", "2년", "3년")) {
            types.add("전문대");
        }
        if (containsAny(universityCategory, "대학원", "석사", "박사")) {
            types.add("대학원");
        }
        if (containsAny(universityCategory, "사이버", "방송통신", "원격")) {
            types.add("원격대학");
        }
        
        return types.isEmpty() ? null : String.join(",", types);
    }
    
    /**
     * 지역 제한 추출
     */
    private String extractRegionLimit(String residencyDetail) {
        if (residencyDetail == null || residencyDetail.isEmpty()) return null;
        
        // "전국", "제한없음" → null
        if (containsAny(residencyDetail, "전국", "제한없음", "제한 없음", "무관")) {
            return null;
        }
        
        String[] regions = {
                "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
                "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
        };
        
        Set<String> foundRegions = new LinkedHashSet<>();
        for (String region : regions) {
            if (residencyDetail.contains(region)) {
                foundRegions.add(region);
            }
        }
        
        return foundRegions.isEmpty() ? null : String.join(",", foundRegions);
    }
    
    /**
     * 장학금 유형 판별
     */
    private ScholarshipType detectScholarshipType(String name, String financialAidType, String organization, String productType) {
        String combined = combineTexts(name, financialAidType, organization, productType).toLowerCase();
        
        if (containsAny(combined, "국가장학", "한국장학재단")) {
            return ScholarshipType.NATIONAL;
        }
        if (containsAny(combined, "근로장학", "교내근로", "근로")) {
            return ScholarshipType.WORK_STUDY;
        }
        if (containsAny(combined, "등록금대출", "학자금대출", "취업후상환", "icl")) {
            return ScholarshipType.TUITION_LOAN;
        }
        if (containsAny(combined, "생활비대출", "생활비")) {
            return ScholarshipType.LIVING_LOAN;
        }
        if (containsAny(combined, "지자체", "시청", "군청", "구청", "도청")) {
            return ScholarshipType.LOCAL;
        }
        if (containsAny(combined, "교내", "대학교", "학교")) {
            return ScholarshipType.UNIVERSITY;
        }
        if (containsAny(combined, "기업", "재단", "민간", "장학회")) {
            return ScholarshipType.PRIVATE;
        }
        
        return ScholarshipType.OTHER;
    }
    
    /**
     * 여러 텍스트 합치기
     */
    private String combineTexts(String... texts) {
        StringBuilder sb = new StringBuilder();
        for (String text : texts) {
            if (text != null && !text.isEmpty()) {
                sb.append(text).append(" ");
            }
        }
        return sb.toString().trim();
    }
    
    /**
     * 텍스트에 특정 키워드 포함 여부
     */
    private boolean containsAny(String text, String... keywords) {
        if (text == null) return false;
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
