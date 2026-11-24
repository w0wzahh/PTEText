package com.ptetext.aspect;

public final class SessionContext {

    private static final ThreadLocal<Integer> CURRENT_USER = new ThreadLocal<>();

    private SessionContext() {
    }

    public static void setUserId(int userId) {
        CURRENT_USER.set(userId);
    }

    public static int userId() {
        Integer id = CURRENT_USER.get();
        if (id == null) {
            throw new IllegalStateException("No authenticated user on this thread");
        }
        return id;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
