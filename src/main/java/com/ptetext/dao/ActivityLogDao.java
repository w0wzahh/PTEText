package com.ptetext.dao;

import com.ptetext.db.ConnectionFactory;

/** ptetext_system.activity_log — the black box recorder. Every action lands here. */
public class ActivityLogDao {

    private final ConnectionFactory db;

    public ActivityLogDao(ConnectionFactory db) {
        this.db = db;
    }

    /*
     * TODO — issue #14:
     *
     *   void log(Integer userId, String action, String details)
     *       — must NEVER throw. Logging must never break a feature: catch, warn, move on
     *   List<ActivityLogEntry> listRecent(int limit)
     *
     * Actions so far: REGISTER, LOGIN, LOGIN_FAILED, LOGOUT, MESSAGE_SENT,
     * GROUP_CREATED, CONTACT_ADDED, ATTACHMENT_ADDED
     */
}
