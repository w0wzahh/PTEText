package com.ptetext.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLoggerTest {

    private EntityManager em;
    private AuditLogger logger;

    @BeforeEach
    void setUp() throws Exception {
        em = mock(EntityManager.class);
        logger = new AuditLogger();
        Field f = AuditLogger.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(logger, em);
    }

    @Test
    void logCallsSpLogActionWithParameters() {
        StoredProcedureQuery q = mock(StoredProcedureQuery.class);
        when(em.createStoredProcedureQuery("ptetext_system.sp_log_action")).thenReturn(q);

        logger.log(3, "LOGIN", "user signed in");

        verify(q).registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
        verify(q).registerStoredProcedureParameter("p_action", String.class, ParameterMode.IN);
        verify(q).registerStoredProcedureParameter("p_details", String.class, ParameterMode.IN);
        verify(q).setParameter("p_user_id", 3);
        verify(q).setParameter("p_action", "LOGIN");
        verify(q).setParameter("p_details", "user signed in");
        verify(q).execute();
    }

    @Test
    void logNeverThrowsOnDatabaseFailure() {
        when(em.createStoredProcedureQuery("ptetext_system.sp_log_action"))
                .thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> logger.log(3, "LOGIN", "details"));
    }
}
