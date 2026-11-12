package com.ptetext.controller;

import com.ptetext.dto.Dtos.UserDto;
import com.ptetext.repository.users.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    @Operation(summary = "List all registered users")
    public List<UserDto> all() {
        return users.findAll().stream()
                .map(u -> new UserDto(u.getUserId(), u.getUsername(), u.getDisplayName(),
                        u.getCreatedAt(), u.getLastSeen()))
                .toList();
    }
}
