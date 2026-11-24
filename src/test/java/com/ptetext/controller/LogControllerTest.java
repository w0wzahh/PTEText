package com.ptetext.controller;

import com.ptetext.entity.system.ActivityLogEntity;
import com.ptetext.repository.system.ActivityLogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogControllerTest {

    private ActivityLogRepository repo;
    private LogController controller;

    @BeforeEach
    void setUp() {
        repo = mock(ActivityLogRepository.class);
        controller = new LogController(repo);
    }

    @Test
    void getRecentLogsReturnsLogs() {
        ActivityLogEntity a = new ActivityLogEntity(1, "LOGIN", "from web");
        when(repo.findTop10ByOrderByLogIdDesc()).thenReturn(List.of(a));

        ResponseEntity<?> res = controller.getRecentLogs("tok");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(List.of(a), res.getBody());
    }

    @Test
    void getRecentLogsReturnsEmptyListWhenNoActivity() {
        when(repo.findTop10ByOrderByLogIdDesc()).thenReturn(List.of());

        ResponseEntity<?> res = controller.getRecentLogs("tok");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(List.of(), res.getBody());
    }
}
