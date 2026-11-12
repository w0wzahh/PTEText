package com.ptetext.controller;

import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.entity.system.ActivityLogEntity;
import com.ptetext.repository.system.ActivityLogRepository;
import com.ptetext.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Activity Log", description = "System activity logs recorder")
public class LogController {

    private final ActivityLogRepository activityLogRepository;
    private final AuthService auth;

    public LogController(ActivityLogRepository activityLogRepository, AuthService auth) {
        this.activityLogRepository = activityLogRepository;
        this.auth = auth;
    }

    @GetMapping
    public ResponseEntity<?> getRecentLogs(@RequestParam String sessionId) {
        if (auth.validateSession(sessionId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Session invalid or expired"));
        }
        List<ActivityLogEntity> logs = activityLogRepository.findTop10ByOrderByLogIdDesc();
        return ResponseEntity.ok(logs);
    }
}
