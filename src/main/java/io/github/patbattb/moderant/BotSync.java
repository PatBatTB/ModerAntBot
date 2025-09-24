package io.github.patbattb.moderant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BotSync implements LongPollingUpdateConsumer {

    private final TelegramClient botClient;
    private final ExecutorService executorService;
    private final UpdateHandler updateHandler;
    private final Logger log = LogManager.getLogger(BotSync.class);
    private static final Object SEND_LOCK = new Object();
    private static final int DEFAULT_SEND_PERIOD = 60;
    private final Semaphore sendSemaphore = new Semaphore(20, true);

    public BotSync(String botToken) {
        executorService = Executors.newFixedThreadPool(100);
        botClient = new OkHttpTelegramClient(botToken);
        updateHandler = new UpdateHandler(this);
    }

    public void consume(List<Update> updates) {
        for (Update update : updates) {
            executorService.submit(() -> updateHandler.handle(update));
        }
    }

    public <T extends Serializable> T execute(BotApiMethod<T> method) throws TelegramApiException, InterruptedException {
        if (method instanceof DeleteMessage) {
            return executeDelete(method);
        }
        sendSemaphore.acquire();
        synchronized (SEND_LOCK) {
            executorService.execute(this::waitForRelease);
            return botClient.execute(method);
        }
    }

    private <T extends Serializable> T executeDelete(BotApiMethod<T> deleteMethod) throws TelegramApiException {
        synchronized (SEND_LOCK) {
            return botClient.execute(deleteMethod);
        }
    }
    
    public Message execute(SendDocument document) throws TelegramApiException, InterruptedException {
        sendSemaphore.acquire();
        synchronized (SEND_LOCK) {
            executorService.execute(this::waitForRelease);
            return botClient.execute(document);
        }
    }

    private void waitForRelease() {
        try {
            TimeUnit.SECONDS.sleep(DEFAULT_SEND_PERIOD);
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            sendSemaphore.release();
        }
    }
}
