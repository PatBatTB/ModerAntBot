package io.github.patbattb.moderant.service;

import io.github.patbattb.moderant.BotSync;
import io.github.patbattb.moderant.database.MessageDelete;
import io.github.patbattb.moderant.database.MessageDeleteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class DeletingService {

    private final int repeatingMillis;
    private final BotSync bot;
    private final Logger log = LogManager.getLogger(DeletingService.class);

    public DeletingService(int repeatingMillis, BotSync bot) {
        this.repeatingMillis = repeatingMillis;
        this.bot = bot;
    }

    public void runRepeatableDeleting() {
        Timer timer = new Timer("DeleteMessagesTimer", true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                deleteMessages();
            }
        };
        timer.scheduleAtFixedRate(task, 0, repeatingMillis);
    }

    private void deleteMessages() {
        MessageDeleteService dbService = new MessageDeleteService();
        List<MessageDelete> messages = new ArrayList<>();
        try {
            messages = dbService.getMessagesForDelete((int) Instant.now().getEpochSecond());
        } catch (SQLException e) {
            log.error(e);
        }
        HashMap<Long, List<Integer>> chatMap = new HashMap<>();
        for(MessageDelete message: messages) {
            List<Integer> messageIds = chatMap.getOrDefault(message.getChatId(), new ArrayList<>());
            messageIds.add(message.getMessageId());
            chatMap.put(message.getChatId(), messageIds);
        }
        for (Map.Entry<Long, List<Integer>> entry: chatMap.entrySet()) {
            DeleteMessages deleteMessages = new DeleteMessages(entry.getKey().toString(), entry.getValue());
            try {
                bot.execute(deleteMessages);
                for (Integer id: entry.getValue()) {
                    dbService.delete(id, entry.getKey());
                }
            } catch (TelegramApiException | InterruptedException | SQLException e) {
                log.error(e);
            }
        }
    }
}
