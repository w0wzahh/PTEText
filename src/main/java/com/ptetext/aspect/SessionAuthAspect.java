package com.ptetext.aspect;

import com.ptetext.dto.Dtos.ErrorResponse;
import com.ptetext.service.AuthService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
public class SessionAuthAspect {

    private final AuthService auth;

    public SessionAuthAspect(AuthService auth) {
        this.auth = auth;
    }

    @Around("@annotation(com.ptetext.aspect.RequiresSession)")
    public Object gateAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        String sessionId = findSessionId(joinPoint);
        Optional<Integer> userId = sessionId == null
                ? Optional.empty()
                : auth.validateSession(sessionId);
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Session invalid or expired"));
        }
        SessionContext.setUserId(userId.get());
        try {
            return joinPoint.proceed();
        } finally {
            SessionContext.clear();
        }
    }

    private String findSessionId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String s && "sessionId".equals(names[i])) {
                return s;
            }
            if (arg != null) {
                try {
                    Method accessor = arg.getClass().getMethod("sessionId");
                    if (accessor.invoke(arg) instanceof String s) {
                        return s;
                    }
                } catch (NoSuchMethodException ignored) {
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(
                            "Could not read sessionId from " + arg.getClass().getSimpleName(), e);
                }
            }
        }
        return null;
    }
}
