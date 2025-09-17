package io.github.patbattb.moderant.database;

public final class SqlParams {
    public static final String DB_FILE_NAME = "test.db";
    public static final int DEFAULT_QUERY_TIMEOUT = 5;

    public static final class UserMutingTable {
        public static final String TABLE_NAME = "user_muting";
        public static final String USER_ID_FIELD = "user_id";
        public static final String TOPIC_ID_FIELD = "topic_id";
        public static final String UNMUTE_TIME_FIELD = "unmute_time";
    }

    public static final class MessageDeleteTable {
        public static final String TABLE_NAME = "message_delete";
        public static final String MESSAGE_ID_FIELD = "message_id";
        public static final String CHAT_ID_FIELD = "chat_id";
        public static final String DELETE_TIME_FIELD = "delete_time";
    }
}
