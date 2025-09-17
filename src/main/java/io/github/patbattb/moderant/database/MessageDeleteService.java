package io.github.patbattb.moderant.database;

import java.sql.SQLException;
import java.util.List;

public class MessageDeleteService {

    private final MessageDeleteDAO dao = new MessageDeleteDAO();

    public List<MessageDelete> getMessagesForDelete(int unixTime) throws SQLException {
        return dao.getMessagesForDelete(unixTime);
    }

    public boolean insert(int messageId, long chatId, int deleteTime) throws SQLException {
        return dao.insert(messageId, chatId, deleteTime);
    }

    public boolean delete(int messageId, long chatId) throws SQLException {
        return dao.delete(messageId, chatId);
    }
}
