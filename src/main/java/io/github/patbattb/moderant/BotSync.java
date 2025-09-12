package io.github.patbattb.moderant;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotSync implements LongPollingUpdateConsumer {

    private final TelegramClient botClient;
    private final ExecutorService executorService;
    private final UpdateHandler updateHandler;
    private static final Object SEND_LOCK = new Object();

    public BotSync(String botToken) {
        executorService = Executors.newFixedThreadPool(10);
        botClient = new OkHttpTelegramClient(botToken);
        updateHandler = new UpdateHandler(this);
    }

    public void consume(List<Update> updates) {
        for (Update update : updates) {
            executorService.submit(() -> updateHandler.handle(update));
        }
    }

    public <T extends Serializable> T execute(BotApiMethod<T> method) {
        synchronized (SEND_LOCK) {
            try {
                Thread.sleep(50);
                return botClient.execute(method);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
    
    public Message execute(SendDocument document) {
        synchronized (SEND_LOCK) {
            try {
                Thread.sleep(50);
                return botClient.execute(document);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
