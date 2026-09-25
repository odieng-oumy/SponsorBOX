package com.sponsorbox.controllers;

import com.sponsorbox.models.CreatorProfile;
import com.sponsorbox.models.User;
import com.sponsorbox.models.UserRole;
import com.sponsorbox.security.JwtUtil;
import com.sponsorbox.services.CreatorService;
import com.sponsorbox.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CreatorService creatorService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String role = request.get("role");

        User user = userService.register(email, password, UserRole.valueOf(role.toUpperCase()));
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "message", "Inscription reussie",
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/register-creator")
    public ResponseEntity<?> registerCreator(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String tiktokHandle = request.get("tiktokHandle");
        String bio = request.getOrDefault("bio", "");
        String category = request.getOrDefault("category", "");

        String handle = tiktokHandle.startsWith("@") ? tiktokHandle : "@" + tiktokHandle;

        User user = userService.register(email, password, UserRole.CREATOR);

        try {
            CreatorProfile profile = creatorService.createProfile(
                    user.getId(), handle, 0, 0.0, bio, category
            );

            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

            return ResponseEntity.ok(Map.of(
                    "message", "Inscription reussie ! Ajoutez le code dans votre bio TikTok pour etre verifie.",
                    "token", token,
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "role", "CREATOR",
                    "tiktokHandle", handle,
                    "verificationCode", profile.getVerificationCode(),
                    "certificationStatus", profile.getCertificationStatus().name()
            ));
        } catch (Exception e) {
            userService.deleteUser(user.getId());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userService.login(email, password);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "message", "Connexion reussie",
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));
    }
}
