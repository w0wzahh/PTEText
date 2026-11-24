package com.ptetext.aspect;

import com.ptetext.dto.Dtos;
import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.service.AuthService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionAuthAspectTest {

    private AuthService auth;
    private SessionAuthAspect aspect;
    private ProceedingJoinPoint pjp;
    private MethodSignature sig;

    @BeforeEach
    void setUp() {
        auth = mock(AuthService.class);
        aspect = new SessionAuthAspect(auth);
        pjp = mock(ProceedingJoinPoint.class);
        sig = mock(MethodSignature.class);
        when(pjp.getSignature()).thenReturn(sig);
    }

    @AfterEach
    void tearDown() {
        SessionContext.clear();
    }

    private void params(String[] names, Object[] args) {
        when(sig.getParameterNames()).thenReturn(names);
        when(pjp.getArgs()).thenReturn(args);
    }

    @Test
    void validSessionProceedsAndExposesUserId() throws Throwable {
        params(new String[]{"sessionId"}, new Object[]{"tok"});
        when(auth.validateSession("tok")).thenReturn(Optional.of(7));
        when(pjp.proceed()).thenAnswer(inv -> {
            assertEquals(7, SessionContext.userId());
            return ResponseEntity.ok("ok");
        });

        Object res = aspect.gateAccess(pjp);

        assertEquals(HttpStatus.OK, ((ResponseEntity<?>) res).getStatusCode());
        verify(pjp).proceed();
        assertThrows(IllegalStateException.class, SessionContext::userId);
    }

    @Test
    void invalidSessionReturns401WithoutProceeding() throws Throwable {
        params(new String[]{"sessionId"}, new Object[]{"bad"});
        when(auth.validateSession("bad")).thenReturn(Optional.empty());

        Object res = aspect.gateAccess(pjp);

        ResponseEntity<?> response = (ResponseEntity<?>) res;
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getBody());
        assertEquals("Session invalid or expired", body.error());
        verify(pjp, never()).proceed();
    }

    @Test
    void sessionIdInsideBodyRecordIsFound() throws Throwable {
        params(new String[]{"req"}, new Object[]{new Dtos.SessionRequest("tok")});
        when(auth.validateSession("tok")).thenReturn(Optional.of(3));
        when(pjp.proceed()).thenReturn(ResponseEntity.ok("ok"));

        Object res = aspect.gateAccess(pjp);

        assertEquals(HttpStatus.OK, ((ResponseEntity<?>) res).getStatusCode());
        verify(pjp).proceed();
    }

    @Test
    void missingSessionIdFailsClosed() throws Throwable {
        params(new String[]{"conversationId"}, new Object[]{5});

        Object res = aspect.gateAccess(pjp);

        assertEquals(HttpStatus.UNAUTHORIZED, ((ResponseEntity<?>) res).getStatusCode());
        verify(pjp, never()).proceed();
        verify(auth, never()).validateSession(anyString());
    }

    @Test
    void contextClearedEvenWhenProceedThrows() throws Throwable {
        params(new String[]{"sessionId"}, new Object[]{"tok"});
        when(auth.validateSession("tok")).thenReturn(Optional.of(7));
        when(pjp.proceed()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> aspect.gateAccess(pjp));
        assertThrows(IllegalStateException.class, SessionContext::userId);
    }
}
