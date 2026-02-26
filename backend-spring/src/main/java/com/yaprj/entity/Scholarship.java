package com.yaprj.entity;

import com.yaprj.entity.enums.ScholarshipType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scholarships")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Scholarship {
    
    @Id
    @Column(length = 36)
    private String id;
    
    // ===== CSV 원본 데이터 (22개 컬럼 그대로 저장) =====
    
    @Column(name = "csv_row_number")
    private Integer csvRowNumber;  // 번호
    
    @Column(name = "organization", nullable = false, length = 300)
    private String organization;  // 운영기관명
    
    @Column(name = "name", nullable = false, length = 500)
    private String name;  // 상품명
    
    @Column(name = "organization_type", length = 100)
    private String organizationType;  // 운영기관구분
    
    @Column(name = "product_type", length = 100)
    private String productType;  // 상품구분
    
    @Column(name = "financial_aid_type", length = 100)
    private String financialAidType;  // 학자금유형구분
    
    @Column(name = "university_category", columnDefinition = "TEXT")
    private String universityCategory;  // 대학구분
    
    @Column(name = "grade_semester", columnDefinition = "TEXT")
    private String gradeSemester;  // 학년구분
    
    @Column(name = "major_category", columnDefinition = "TEXT")
    private String majorCategory;  // 학과구분
    
    @Column(name = "grade_criteria", columnDefinition = "TEXT")
    private String gradeCriteria;  // 성적기준 상세내용
    
    @Column(name = "income_criteria", columnDefinition = "TEXT")
    private String incomeCriteria;  // 소득기준 상세내용
    
    @Column(name = "support_details", columnDefinition = "TEXT")
    private String supportDetails;  // 지원내역 상세내용
    
    @Column(name = "special_qualification", columnDefinition = "TEXT")
    private String specialQualification;  // 특정자격 상세내용
    
    @Column(name = "residency_detail", columnDefinition = "TEXT")
    private String residencyDetail;  // 지역거주여부 상세내용
    
    @Column(name = "selection_method", columnDefinition = "TEXT")
    private String selectionMethod;  // 선발방법 상세내용
    
    @Column(name = "selection_count", columnDefinition = "TEXT")
    private String selectionCount;  // 선발인원 상세내용
    
    @Column(name = "eligibility_restriction", columnDefinition = "TEXT")
    private String eligibilityRestriction;  // 자격제한 상세내용
    
    @Column(name = "recommendation_required", columnDefinition = "TEXT")
    private String recommendationRequired;  // 추천필요여부 상세내용
    
    @Column(name = "required_documents", columnDefinition = "TEXT")
    private String requiredDocuments;  // 제출서류 상세내용
    
    @Column(name = "website_url", length = 1000)
    private String websiteUrl;  // 홈페이지 주소
    
    @Column(name = "apply_start")
    private LocalDate applyStart;  // 모집시작일
    
    @Column(name = "apply_end")
    private LocalDate applyEnd;  // 모집종료일
    
    // ===== 파싱된 정량 데이터 (매칭에 사용) =====
    
    @Enumerated(EnumType.STRING)
    @Column(name = "scholarship_type")
    @Builder.Default
    private ScholarshipType scholarshipType = ScholarshipType.OTHER;
    
    // 최소 성적 (4.5 만점 기준, null이면 미파싱)
    @Column(name = "min_gpa", precision = 3, scale = 2)
    private BigDecimal minGpa;
    
    // 최대 소득분위 (1~10, null이면 미파싱)
    @Column(name = "max_income_level")
    private Integer maxIncomeLevel;
    
    // 허용 학적상태 (enrolled,expected,leave 형태)
    @Column(name = "allowed_academic_status", length = 100)
    private String allowedAcademicStatus;
    
    // 허용 학년 (1,2,3,4 형태)
    @Column(name = "allowed_grades", length = 50)
    private String allowedGrades;
    
    // 허용 대학유형 (4년제,전문대,대학원 등)
    @Column(name = "allowed_university_types", length = 200)
    private String allowedUniversityTypes;
    
    // 지역 제한 키워드 (서울,경기 등)
    @Column(name = "region_limit", length = 200)
    private String regionLimit;
    
    // ===== 관리용 필드 =====
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
