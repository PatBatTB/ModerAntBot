package io.github.patbattb.moderant.database;

import java.sql.*;
import java.util.Optional;

import static io.github.patbattb.moderant.database.SqlParams.UserMutingTable.*;

public class UserMutingDAO {

    public Optional<UserMuting> get(long userId, Integer topicId) throws SQLException {
        String sql = String.format("SELECT * FROM %s WHERE %s = ? AND %s = ?",
                TABLE_NAME, USER_ID_FIELD, TOPIC_ID_FIELD);
        try (Connection connection = SQLiteConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(SqlParams.DEFAULT_QUERY_TIMEOUT);
            statement.setLong(1, userId);
            statement.setInt(2, topicId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new UserMuting(
                            resultSet.getLong(USER_ID_FIELD),
                            resultSet.getInt(TOPIC_ID_FIELD),
                            resultSet.getInt(UNMUTE_TIME_FIELD))
                    );
                }
                return Optional.empty();
            }
        }
    }

    public boolean insert(long userId, Integer topicId, int unmuteTime) throws SQLException {
        String sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                TABLE_NAME, USER_ID_FIELD, TOPIC_ID_FIELD, UNMUTE_TIME_FIELD);
        try (Connection connection = SQLiteConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(SqlParams.DEFAULT_QUERY_TIMEOUT);
            statement.setLong(1, userId);
            statement.setInt(2, topicId);
            statement.setInt(3, unmuteTime);
            int affectedRow = statement.executeUpdate();
            return affectedRow > 0;
        }
    }

    public boolean update (long userId, int topicId, int newUnmuteTime) throws SQLException {
        String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
                TABLE_NAME, UNMUTE_TIME_FIELD, USER_ID_FIELD, TOPIC_ID_FIELD);
        try (Connection connection = SQLiteConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(SqlParams.DEFAULT_QUERY_TIMEOUT);
            statement.setInt(1, newUnmuteTime);
            statement.setLong(2, userId);
            statement.setInt(3, topicId);
            int affectedRow = statement.executeUpdate();
            return affectedRow > 0;
        }
    }

}
