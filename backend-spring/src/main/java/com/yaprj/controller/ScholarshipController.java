package com.yaprj.controller;

import com.yaprj.dto.request.ScholarshipCheckRequest;
import com.yaprj.dto.response.ScholarshipCheckResponse;
import com.yaprj.entity.enums.ScholarshipType;
import com.yaprj.service.ScholarshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scholarships")
@RequiredArgsConstructor
public class ScholarshipController {
    
    private final ScholarshipService scholarshipService;
    
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getScholarships(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ScholarshipType scholarshipType,
            @RequestParam(defaultValue = "false") boolean onlyAccepting) {
        return ResponseEntity.ok(scholarshipService.getScholarships(
                page, perPage, search, scholarshipType, onlyAccepting));
    }
    
    @PostMapping("/check")
    public ResponseEntity<ScholarshipCheckResponse> checkEligibility(
            @Valid @RequestBody ScholarshipCheckRequest request) {
        return ResponseEntity.ok(scholarshipService.checkEligibility(request));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedScholarships() {
        List<Map<String, Object>> scholarships = scholarshipService.getFeaturedScholarships();
        return ResponseEntity.ok(Map.of(
                "scholarships", scholarships,
                "total", scholarships.size()
        ));
    }
    
    @GetMapping("/accepting")
    public ResponseEntity<Map<String, Object>> getAcceptingScholarships() {
        List<Map<String, Object>> scholarships = scholarshipService.getAcceptingScholarships();
        return ResponseEntity.ok(Map.of(
                "scholarships", scholarships,
                "total", scholarships.size()
        ));
    }
    
    @GetMapping("/{scholarshipId}")
    public ResponseEntity<Map<String, Object>> getScholarshipDetail(@PathVariable String scholarshipId) {
        return ResponseEntity.ok(scholarshipService.getScholarshipDetail(scholarshipId));
    }
}
