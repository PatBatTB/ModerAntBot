package io.github.patbattb.moderant.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static io.github.patbattb.moderant.database.SqlParams.*;

public class SQLiteConnectionPool {

    private static final HikariDataSource dataSource;
    private static final Logger LOG = LogManager.getLogger(SQLiteConnectionPool.class);

    static {
        HikariConfig config = new HikariConfig();
        String urlString = "jdbc:sqlite:" + DB_FILE_NAME;
        config.setJdbcUrl(urlString);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        config.addDataSourceProperty("busy_timeout", "5000");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(DEFAULT_QUERY_TIMEOUT);
            statement.execute(String.format("""
                    CREATE TABLE IF NOT EXISTS %s (
                    %s BIGINT,
                    %s INTEGER,
                    %s TEXT NOT NULL,
                    PRIMARY KEY (%s, %s))
                    """, UserMutingTable.TABLE_NAME, UserMutingTable.USER_ID_FIELD, UserMutingTable.TOPIC_ID_FIELD,
                    UserMutingTable.UNMUTE_TIME_FIELD, UserMutingTable.USER_ID_FIELD, UserMutingTable.TOPIC_ID_FIELD));
            statement.execute(String.format("""
                    CREATE TABLE IF NOT EXISTS %s (
                    %s INTEGER,
                    %s INTEGER,
                    %s TEXT NOT NULL,
                    PRIMARY KEY (%s, %s))
                    """, MessageDeleteTable.TABLE_NAME, MessageDeleteTable.MESSAGE_ID_FIELD, MessageDeleteTable.TOPIC_ID_FIELD,
                    MessageDeleteTable.DELETE_TIME_FIELD, MessageDeleteTable.MESSAGE_ID_FIELD, MessageDeleteTable.TOPIC_ID_FIELD));
            statement.execute(String.format("""
                    CREATE INDEX IF NOT EXISTS idx_%s_%s ON %s(%s)
                    """, MessageDeleteTable.TABLE_NAME, MessageDeleteTable.DELETE_TIME_FIELD,
                    MessageDeleteTable.TABLE_NAME, MessageDeleteTable.DELETE_TIME_FIELD));
        }
    }
}
