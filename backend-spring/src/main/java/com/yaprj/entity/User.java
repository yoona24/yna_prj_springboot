package com.yaprj.entity;

import com.yaprj.entity.enums.AcademicStatus;
import com.yaprj.entity.enums.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(length = 100)
    private String name;
    
    @Column(name = "profile_image", length = 500)
    private String profileImage;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;
    
    @Column(name = "provider_id", nullable = false)
    private String providerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status")
    private AcademicStatus academicStatus;
    
    private Integer grade;
    
    @Column(name = "birth_year")
    private Integer birthYear;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;
    
    @Column(name = "income_level")
    private Integer incomeLevel;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScholarshipResult> scholarshipResults = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
