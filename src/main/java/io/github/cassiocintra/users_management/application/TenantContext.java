package io.github.cassiocintra.users_management.application;

public class TenantContext {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> WORKSPACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();

    private TenantContext() {}

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static void setWorkspaceId(String workspaceId) {
        WORKSPACE_ID.set(workspaceId);
    }

    public static String getWorkspaceId() {
        return WORKSPACE_ID.get();
    }

    public static void setUserEmail(String email) {
        USER_EMAIL.set(email);
    }

    public static String getUserEmail() {
        return USER_EMAIL.get();
    }

    public static void setUserName(String name) {
        USER_NAME.set(name);
    }

    public static String getUserName() {
        return USER_NAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        WORKSPACE_ID.remove();
        USER_EMAIL.remove();
        USER_NAME.remove();
    }
}
