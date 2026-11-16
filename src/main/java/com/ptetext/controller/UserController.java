package com.ptetext.controller;

import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.dto.Dtos.UserDto;
import com.ptetext.repository.users.UserRepository;
import com.ptetext.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserRepository users;
    private final AuthService auth;

    public UserController(UserRepository users, AuthService auth) {
        this.users = users;
        this.auth = auth;
    }

    @GetMapping
    @Operation(summary = "List all registered users")
    public ResponseEntity<?> all(@RequestParam String sessionId) {
        if (auth.validateSession(sessionId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Session invalid or expired"));
        }
        List<UserDto> body = users.findAll().stream()
                .map(u -> new UserDto(u.getUserId(), u.getUsername(), u.getDisplayName(),
                        u.getCreatedAt(), u.getLastSeen()))
                .toList();
        return ResponseEntity.ok(body);
    }
}
