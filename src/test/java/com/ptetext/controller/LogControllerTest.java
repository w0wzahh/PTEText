package com.ptetext.controller;

import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.entity.system.ActivityLogEntity;
import com.ptetext.repository.system.ActivityLogRepository;
import com.ptetext.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogControllerTest {

    private ActivityLogRepository repo;
    private AuthService auth;
    private LogController controller;

    @BeforeEach
    void setUp() {
        repo = mock(ActivityLogRepository.class);
        auth = mock(AuthService.class);
        controller = new LogController(repo, auth);
    }

    @Test
    void getRecentLogsReturnsLogsOnValidSession() {
        ActivityLogEntity a = new ActivityLogEntity(1, "LOGIN", "from web");
        when(auth.validateSession("tok")).thenReturn(Optional.of(1));
        when(repo.findTop10ByOrderByLogIdDesc()).thenReturn(List.of(a));

        ResponseEntity<?> res = controller.getRecentLogs("tok");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(List.of(a), res.getBody());
    }

    @Test
    void getRecentLogsRejectsInvalidSession() {
        when(auth.validateSession("bad")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.getRecentLogs("bad");

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        ErrorResponse body = assertInstanceOf(ErrorResponse.class, res.getBody());
        assertEquals("Session invalid or expired", body.error());
        verify(repo, never()).findTop10ByOrderByLogIdDesc();
    }

    @Test
    void getRecentLogsReturnsEmptyListWhenNoActivity() {
        when(auth.validateSession("tok")).thenReturn(Optional.of(1));
        when(repo.findTop10ByOrderByLogIdDesc()).thenReturn(List.of());

        ResponseEntity<?> res = controller.getRecentLogs("tok");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(List.of(), res.getBody());
    }
}
