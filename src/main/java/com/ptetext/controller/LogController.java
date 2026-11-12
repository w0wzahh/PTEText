package com.ptetext.controller;

import com.ptetext.entity.system.ActivityLogEntity;
import com.ptetext.repository.system.ActivityLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Activity Log", description = "System activity logs recorder")
public class LogController {

    private final ActivityLogRepository activityLogRepository;

    public LogController(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogEntity>> getRecentLogs() {
        List<ActivityLogEntity> logs = activityLogRepository.findTop10ByOrderByLogIdDesc();
        return ResponseEntity.ok(logs);
    }
}