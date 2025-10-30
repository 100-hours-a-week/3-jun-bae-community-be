package com.ktb.community.controller;

import com.ktb.community.dto.auth.AuthResponse;
import com.ktb.community.dto.auth.LoginRequest;
import com.ktb.community.entity.User;
import com.ktb.community.service.UserService;
import com.ktb.community.session.Session;
import com.ktb.community.session.SessionProvider;
import com.ktb.community.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static com.ktb.community.support.CookieUtil.deleteSessionCookie;
import static com.ktb.community.support.CookieUtil.makeSessionCookie;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final SessionProvider sessionProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.getByEmailOrThrow(request.email());

            if (!passwordEncoder.matches(request.password(), user.getPassword())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid username or password");
            }

            userService.updateLastLogin(user);

            List<String> roles = new ArrayList<>();
            roles.add(UserRole.USER.name());

            if (user.isAdmin()){
                roles.add(UserRole.ADMIN.name());
            }
            Session session = sessionProvider.createSession();

            session.setAttr("userId", user.getId());
            session.setAttr("roles", roles);

            ResponseCookie sessionCookie = makeSessionCookie(session.getId(), false);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                    .body(AuthResponse.from(user, roles));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestAttribute Long userId) {
        if (userId != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }
        ResponseCookie removerCookie = deleteSessionCookie(false);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, removerCookie.toString())
                .build();
    }
}
