package com.ptetext.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    @PersistenceContext(unitName = "system")
    private EntityManager em;

    public void log(Integer userId, String action, String details) {
        try {
            StoredProcedureQuery q = em.createStoredProcedureQuery("ptetext_system.sp_log_action");
            q.registerStoredProcedureParameter("p_user_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_action", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_details", String.class, ParameterMode.IN);
            q.setParameter("p_user_id", userId);
            q.setParameter("p_action", action);
            q.setParameter("p_details", details);
            q.execute();
        } catch (RuntimeException e) {
            System.err.println("Warning: could not write activity log: " + e.getMessage());
        }
    }
}
