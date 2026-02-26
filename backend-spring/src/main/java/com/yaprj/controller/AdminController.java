package com.yaprj.controller;

import com.yaprj.dto.request.ScholarshipCreateRequest;
import com.yaprj.dto.request.ScholarshipUpdateRequest;
import com.yaprj.dto.response.CsvUploadResponse;
import com.yaprj.dto.response.DashboardStatsResponse;
import com.yaprj.dto.response.ScholarshipResponse;
import com.yaprj.entity.enums.ScholarshipType;
import com.yaprj.service.CsvParserService;
import com.yaprj.service.ScholarshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final ScholarshipService scholarshipService;
    private final CsvParserService csvParserService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboard() {
        return ResponseEntity.ok(scholarshipService.getDashboardStats());
    }
    
    @PostMapping("/upload-csv")
    public ResponseEntity<CsvUploadResponse> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "append") String mode,
            @AuthenticationPrincipal String adminId) {
        return ResponseEntity.ok(csvParserService.parseAndSave(file, mode, adminId));
    }
    
    @GetMapping("/scholarships")
    public ResponseEntity<Map<String, Object>> getScholarships(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ScholarshipType type,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFeatured) {
        return ResponseEntity.ok(scholarshipService.getAdminScholarships(
                page, perPage, search, type, isActive, isFeatured));
    }
    
    @GetMapping("/scholarships/{id}")
    public ResponseEntity<ScholarshipResponse> getScholarship(@PathVariable String id) {
        return ResponseEntity.ok(scholarshipService.getAdminScholarshipDetail(id));
    }
    
    @PostMapping("/scholarships")
    public ResponseEntity<ScholarshipResponse> createScholarship(@RequestBody ScholarshipCreateRequest request) {
        return ResponseEntity.ok(scholarshipService.createScholarship(request));
    }
    
    @PutMapping("/scholarships/{id}")
    public ResponseEntity<ScholarshipResponse> updateScholarship(
            @PathVariable String id,
            @RequestBody ScholarshipUpdateRequest request) {
        return ResponseEntity.ok(scholarshipService.updateScholarship(id, request));
    }
    
    @DeleteMapping("/scholarships/{id}")
    public ResponseEntity<Map<String, String>> deleteScholarship(@PathVariable String id) {
        scholarshipService.deleteScholarship(id);
        return ResponseEntity.ok(Map.of("message", "장학금이 삭제되었습니다."));
    }
    
    @DeleteMapping("/scholarships/all")
    public ResponseEntity<Map<String, Object>> deleteAllScholarships() {
        int count = scholarshipService.deleteAllScholarships();
        return ResponseEntity.ok(Map.of(
                "message", "모든 장학금이 삭제되었습니다.",
                "deleted_count", count
        ));
    }
    
    @PostMapping("/scholarships/deactivate-all")
    public ResponseEntity<Map<String, Object>> deactivateAllScholarships() {
        int count = scholarshipService.deactivateAllScholarships();
        return ResponseEntity.ok(Map.of(
                "message", "모든 장학금이 비활성화되었습니다.",
                "deactivated_count", count
        ));
    }
    
    @DeleteMapping("/scholarships/inactive")
    public ResponseEntity<Map<String, Object>> deleteInactiveScholarships() {
        int count = scholarshipService.deleteInactiveScholarships();
        return ResponseEntity.ok(Map.of(
                "message", "비활성 장학금이 삭제되었습니다.",
                "deleted_count", count
        ));
    }
    
    @PostMapping("/scholarships/bulk-update")
    public ResponseEntity<Map<String, String>> bulkUpdateScholarships(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) request.get("ids");
        Boolean isActive = (Boolean) request.get("is_active");
        Boolean isFeatured = (Boolean) request.get("is_featured");
        
        scholarshipService.bulkUpdateScholarships(ids, isActive, isFeatured);
        return ResponseEntity.ok(Map.of("message", "일괄 수정되었습니다."));
    }
}
