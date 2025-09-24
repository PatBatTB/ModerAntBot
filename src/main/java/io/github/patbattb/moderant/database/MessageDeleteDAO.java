package io.github.patbattb.moderant.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static io.github.patbattb.moderant.database.SqlParams.MessageDeleteTable.*;

public class MessageDeleteDAO {

    List<MessageDelete> getMessagesForDelete(Integer unixTime) throws SQLException {
        String sql = String.format("SELECT * FROM %s WHERE %s < ?",
                TABLE_NAME, DELETE_TIME_FIELD);
        try (Connection connection = SQLiteConnectionPool.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            List<MessageDelete> resultList = new ArrayList<>();
            statement.setQueryTimeout(SqlParams.DEFAULT_QUERY_TIMEOUT);
            statement.setInt(1, unixTime);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultList.add(
                            new MessageDelete(
                                    resultSet.getInt(MESSAGE_ID_FIELD),
                                    resultSet.getLong(CHAT_ID_FIELD),
                                    resultSet.getInt(DELETE_TIME_FIELD)
                            )
                    );
                }
            }
            return resultList;
        }
    }

    boolean insert(int messageId, long chatId, int deleteTime) throws SQLException {
        String sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                TABLE_NAME, MESSAGE_ID_FIELD, CHAT_ID_FIELD, DELETE_TIME_FIELD);
        try (Connection connection = SQLiteConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(SqlParams.DEFAULT_QUERY_TIMEOUT);
            statement.setInt(1, messageId);
            statement.setLong(2, chatId);
            statement.setInt(3, deleteTime);
            int affectedRow = statement.executeUpdate();
            return affectedRow > 0;
        }
    }

    boolean delete(int messageId, long chatId) throws SQLException {
        String sql = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
                TABLE_NAME, MESSAGE_ID_FIELD, CHAT_ID_FIELD);
        try(Connection connection = SQLiteConnectionPool.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(SqlParams.DEFAULT_QUERY_TIMEOUT);
            statement.setInt(1, messageId);
            statement.setLong(2, chatId);
            int affectedRow = statement.executeUpdate();
            return affectedRow > 0;
        }
    }

}
