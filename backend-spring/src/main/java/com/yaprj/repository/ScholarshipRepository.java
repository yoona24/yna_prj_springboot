package com.yaprj.repository;

import com.yaprj.entity.Scholarship;
import com.yaprj.entity.enums.ScholarshipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, String> {
    
    List<Scholarship> findByIsActiveTrueOrderByIsFeaturedDescUpdatedAtDesc();
    
    Page<Scholarship> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT s FROM Scholarship s WHERE s.isActive = true AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.organization) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Scholarship> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT s FROM Scholarship s WHERE " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.organization) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:type IS NULL OR s.scholarshipType = :type) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) AND " +
           "(:isFeatured IS NULL OR s.isFeatured = :isFeatured)")
    Page<Scholarship> findWithFilters(
            @Param("search") String search,
            @Param("type") ScholarshipType type,
            @Param("isActive") Boolean isActive,
            @Param("isFeatured") Boolean isFeatured,
            Pageable pageable);
    
    List<Scholarship> findByIsFeaturedTrueAndIsActiveTrueOrderByUpdatedAtDesc();
    
    @Query("SELECT s FROM Scholarship s WHERE s.isActive = true AND " +
           "s.applyStart <= :today AND s.applyEnd >= :today ORDER BY s.applyEnd ASC")
    List<Scholarship> findAcceptingApplications(@Param("today") LocalDate today);
    
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    long countByIsFeaturedTrue();
    
    @Query("SELECT s.scholarshipType, COUNT(s) FROM Scholarship s GROUP BY s.scholarshipType")
    List<Object[]> countByScholarshipType();
    
    @Query("SELECT s.organizationType, COUNT(s) FROM Scholarship s GROUP BY s.organizationType")
    List<Object[]> countByOrganizationType();
    
    @Modifying
    @Query("UPDATE Scholarship s SET s.isActive = false WHERE s.isActive = true")
    int deactivateAll();
    
    @Modifying
    @Query("DELETE FROM Scholarship s WHERE s.isActive = false")
    int deleteInactive();
}
