package com.ptetext.controller;

import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.dto.Dtos.LoginRequest;
import com.ptetext.dto.Dtos.LoginResponse;
import com.ptetext.dto.Dtos.RegisterRequest;
import com.ptetext.dto.Dtos.SessionRequest;
import com.ptetext.dto.Dtos.SessionValidation;
import com.ptetext.dto.Dtos.UserDto;
import com.ptetext.entity.users.SessionEntity;
import com.ptetext.entity.users.UserEntity;
import com.ptetext.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Accounts", description = "register, log in, stay logged in")
public class AuthController {

    private final AuthService auth;
    private final com.ptetext.repository.users.UserRepository users;

    public AuthController(AuthService auth,
                          com.ptetext.repository.users.UserRepository users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/register")
    @Operation(summary = "Create an account")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest req) {
        UserEntity u = auth.register(req.username(), req.password(), req.displayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(u));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in -> session token (12h)")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<SessionEntity> session = auth.login(req.username(), req.password());
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid username or password"));
        }
        UserDto user = users.findById(session.get().getUserId())
                .map(this::toDto)
                .orElse(new UserDto(session.get().getUserId(), null, null, null, null));
        return ResponseEntity.ok(
                new LoginResponse(session.get().getSessionId(), user));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate a session token")
    public ResponseEntity<Void> logout(@RequestBody SessionRequest req) {
        auth.logout(req.sessionId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session/{token}")
    @Operation(summary = "Check a session token -> userId")
    public ResponseEntity<?> validate(@PathVariable String token) {
        return auth.validateSession(token)
                .<ResponseEntity<?>>map(id -> ResponseEntity.ok(new SessionValidation(id)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Session invalid or expired")));
    }

    private UserDto toDto(UserEntity u) {
        return new UserDto(u.getUserId(), u.getUsername(), u.getDisplayName(),
                u.getCreatedAt(), u.getLastSeen());
    }

}
