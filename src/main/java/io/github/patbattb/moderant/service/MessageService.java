package io.github.patbattb.moderant.service;

import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

public class MessageService {

    public static String getMessageLink(Message message, Integer recycleTopicId, Integer recycleMessageId) {
        String mask = "https://t.me/c/";
        String groupId = message.getChatId().toString();
        if (groupId.startsWith("-100")) {
            groupId = groupId.substring(4);
        } else if (groupId.startsWith("-")) {
            groupId = groupId.substring(1);
        }
        return mask + groupId + "/" + recycleTopicId + "/" + recycleMessageId;
    }

    public static String escapingString(String text) {
        List<String> symbols = List.of(".", "-", "\"");
        for (String symbol : symbols) {
            if (text.contains(symbol)) {
                text = text.replace(symbol, "\\" + symbol);
            }
        }
        return text;
    }
}
