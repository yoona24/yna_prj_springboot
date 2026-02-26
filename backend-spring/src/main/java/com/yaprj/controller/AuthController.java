package com.yaprj.controller;

import com.yaprj.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    @GetMapping("/kakao/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl + "/auth/callback?token=demo_" + UUID.randomUUID());
    }
    
    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam(required = false) String code, HttpServletResponse response) throws IOException {
        String userId = UUID.randomUUID().toString();
        String token = jwtTokenProvider.createAccessToken(userId, "demo@example.com");
        response.sendRedirect(frontendUrl + "/auth/callback?token=" + token);
    }
    
    @GetMapping("/naver/login")
    public void naverLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl + "/auth/callback?token=demo_" + UUID.randomUUID());
    }
    
    @GetMapping("/naver/callback")
    public void naverCallback(@RequestParam(required = false) String code, HttpServletResponse response) throws IOException {
        String userId = UUID.randomUUID().toString();
        String token = jwtTokenProvider.createAccessToken(userId, "demo@example.com");
        response.sendRedirect(frontendUrl + "/auth/callback?token=" + token);
    }
    
    @GetMapping("/google/login")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl + "/auth/callback?token=demo_" + UUID.randomUUID());
    }
    
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam(required = false) String code, HttpServletResponse response) throws IOException {
        String userId = UUID.randomUUID().toString();
        String token = jwtTokenProvider.createAccessToken(userId, "demo@example.com");
        response.sendRedirect(frontendUrl + "/auth/callback?token=" + token);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");
        
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String userId = jwtTokenProvider.getUserId(refreshToken);
            String newAccessToken = jwtTokenProvider.createAccessToken(userId, null);
            return ResponseEntity.ok(Map.of(
                    "access_token", newAccessToken,
                    "token_type", "bearer"
            ));
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe() {
        return ResponseEntity.ok(Map.of(
                "id", "demo-user",
                "email", "demo@example.com",
                "name", "Demo User",
                "provider", "demo"
        ));
    }
}
