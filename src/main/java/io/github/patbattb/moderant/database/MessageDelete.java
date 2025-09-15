package io.github.patbattb.moderant.database;

public class MessageDelete {
    private final int messageId;
    private final long chatId;
    private Integer deleteTime;

    public MessageDelete(int messageId, long chatId, Integer deleteTime) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.deleteTime = deleteTime;
    }

    public int getMessageId() {
        return messageId;
    }

    public long getChatId() {
        return chatId;
    }

    public Integer getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Integer deleteTime) {
        this.deleteTime = deleteTime;
    }
}
