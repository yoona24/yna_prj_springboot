package com.yaprj.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        return ResponseEntity.ok(Map.of(
                "academic_status", "",
                "grade", "",
                "birth_year", "",
                "gpa", "",
                "income_level", ""
        ));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile() {
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }
    
    @DeleteMapping("")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }
}
