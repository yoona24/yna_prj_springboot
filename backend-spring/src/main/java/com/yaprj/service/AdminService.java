package com.yaprj.service;

import com.yaprj.dto.request.AdminLoginRequest;
import com.yaprj.dto.response.AdminLoginResponse;
import com.yaprj.entity.Admin;
import com.yaprj.repository.AdminRepository;
import com.yaprj.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostConstruct
    public void createDefaultAdmin() {
        if (!adminRepository.existsByUsername("admin")) {
            Admin admin = Admin.builder()
                    .id(UUID.randomUUID().toString())
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("1234"))
                    .name("관리자")
                    .isActive(true)
                    .build();
            adminRepository.save(admin);
            log.info("✅ Default admin account created (admin / 1234)");
        }
    }
    
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndIsActiveTrue(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        
        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);
        
        String token = jwtTokenProvider.createAdminToken(admin.getId(), admin.getUsername());
        
        return AdminLoginResponse.builder()
                .accessToken(token)
                .tokenType("bearer")
                .admin(AdminLoginResponse.AdminInfo.builder()
                        .id(admin.getId())
                        .username(admin.getUsername())
                        .name(admin.getName())
                        .build())
                .build();
    }
    
    public Admin getAdminById(String adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
    }
    
    @Transactional
    public void changePassword(String adminId, String currentPassword, String newPassword) {
        Admin admin = getAdminById(adminId);
        
        if (!passwordEncoder.matches(currentPassword, admin.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }
}
