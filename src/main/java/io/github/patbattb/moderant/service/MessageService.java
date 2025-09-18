package io.github.patbattb.moderant.service;

import java.util.List;

public class MessageService {

    private static final List<String> ESCAPING_SYMBOLS = List.of(".", "-", "\"", "_", "*", "[", "]", "(", ")", "`",
            "~", ">", "#", "+", "=", "|", "{", "}", "!");
    private static final String API_MASK = "https://t\\.me/c/";

    public static String getMessageLink(String chatId, Integer recycleTopicId, Integer recycleMessageId) {
        chatId = getHttpChatId(chatId);
        return API_MASK + chatId + "/" + recycleTopicId + "/" + recycleMessageId;
    }

    public static String getTopicLink(String chatId, Integer topicId) {
        chatId = getHttpChatId(chatId);
        return API_MASK + chatId + "/" + topicId;
    }

    public static String escapingString(String text) {
        for (String symbol : ESCAPING_SYMBOLS) {
            if (text.contains(symbol)) {
                text = text.replace(symbol, "\\" + symbol);
            }
        }
        return text;
    }

    private static String getHttpChatId(String chatId) {
        if (chatId.startsWith("-100")) {
            return chatId.substring(4);
        }
        if (chatId.startsWith("-")) {
            return chatId.substring(1);
        }
        return chatId;
    }
}
