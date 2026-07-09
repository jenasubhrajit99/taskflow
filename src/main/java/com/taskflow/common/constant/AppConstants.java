package com.taskflow.common.constant;

public final class AppConstants {

    private AppConstants() {}

    public static final String API_V1 = "/api/v1";

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE   = 20;
    public static final int MAX_PAGE_SIZE       = 100;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX        = "Bearer ";

    public static final class Topics {
        public static final String TASK_EVENTS         = "taskflow.task.events";
        public static final String USER_EVENTS         = "taskflow.user.events";
        public static final String NOTIFICATION_EVENTS = "taskflow.notification.events";

        private Topics() {}
    }

    public static final class CacheNames {
        public static final String USERS      = "users";
        public static final String WORKSPACES = "workspaces";
        public static final String PROJECTS   = "projects";
        public static final String TASKS      = "tasks";

        private CacheNames() {}
    }

    public static final class Roles {
        public static final String ADMIN = "ROLE_ADMIN";
        public static final String USER  = "ROLE_USER";

        private Roles() {}
    }
}
