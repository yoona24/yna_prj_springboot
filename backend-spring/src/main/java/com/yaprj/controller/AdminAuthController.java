package com.yaprj.controller;

import com.yaprj.dto.request.AdminLoginRequest;
import com.yaprj.dto.response.AdminLoginResponse;
import com.yaprj.entity.Admin;
import com.yaprj.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController { //관리자 기능
    
    private final AdminService adminService;
    
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal String adminId) {
        Admin admin = adminService.getAdminById(adminId);
        return ResponseEntity.ok(Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "name", admin.getName() != null ? admin.getName() : "",
                "lastLogin", admin.getLastLogin() != null ? admin.getLastLogin().toString() : null
        ));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal String adminId,
            @RequestBody Map<String, String> request) {
        adminService.changePassword(adminId, request.get("currentPassword"), request.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
