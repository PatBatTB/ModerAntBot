package io.github.patbattb.moderant.service;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public class MessageService {

    public static String getMessageLink(Message message, Integer recycleTopicId, Integer recycleMessageId) {
        //https://t.me/c/3099601601/98/209
        String mask = "https://t.me/c/";
        String groupId = message.getChatId().toString();
        if (groupId.startsWith("-100")) {
            groupId = groupId.substring(4);
        }
        return mask + groupId + "/" + recycleTopicId + "/" + recycleMessageId;
    }
}
