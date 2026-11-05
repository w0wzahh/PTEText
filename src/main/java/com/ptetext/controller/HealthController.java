package com.ptetext.controller;

import com.ptetext.repository.chat.ConversationRepository;
import com.ptetext.repository.system.ActivityLogRepository;
import com.ptetext.repository.users.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "proves all three databases answer")
public class HealthController {

    private final UserRepository users;
    private final ConversationRepository conversations;
    private final ActivityLogRepository activityLog;

    public HealthController(UserRepository users, ConversationRepository conversations,
                            ActivityLogRepository activityLog) {
        this.users = users;
        this.conversations = conversations;
        this.activityLog = activityLog;
    }

    @GetMapping
    @Operation(summary = "Ping each of the three databases")
    public Map<String, String> health() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("ptetext_users", check(() -> users.count()));
        status.put("ptetext_chat", check(() -> conversations.count()));
        status.put("ptetext_system", check(() -> activityLog.count()));
        return status;
    }

    private String check(Check c) {
        try {
            return "OK (" + c.run() + " rows)";
        } catch (RuntimeException e) {
            return "FAIL: " + e.getMessage();
        }
    }

    private interface Check {
        long run();
    }
}
