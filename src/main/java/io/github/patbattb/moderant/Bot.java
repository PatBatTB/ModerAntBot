package io.github.patbattb.moderant;

import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.service.FileService;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Bot implements LongPollingUpdateConsumer {

    private final TelegramClient botClient;

    public Bot(String botToken) {
        this.botClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(this::consume);
    }

    private void consume(Update update) {
        try {
            if (update.hasMessage() && "supergroup".equals(update.getMessage().getChat().getType())) {
                handleGroupMessage(update.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGroupMessage(Message message) {

        // ОЧИСТКА СИСЕТМНЫХ СООБЩЕНИЙ
        if (isSystemMessage(message)) {
            deleteCurrentMessage(message);
            return;
        }
        // ПРОВЕРКА ДОПУСТИМОСТИ СООБЩЕНИЯ
            // необходимо проверить в какой топик отправлено сообщение.
        Integer threadId = message.getMessageThreadId();
            //TODO сравнить ограничение пользователя по времени с разрешениями в данном топике (Нужна БД)

            // сравнить медиа в сообщении пользователя с ограничениями для топика
        ForumTopic topic = Parameters.getTopics().get(threadId);

            //TODO проверить ограничения по смайликам

        // если все ок - пропускаем сообщение
        // если нет:
        if (topic.checkForRestrictions(message)) {
            handleRestrictedMessage(message);
        }
    }

    private void handleRestrictedMessage(Message message) {
        // если есть текст или описание
        if (message.hasText() || message.hasCaption()) {
            // архивируем сообщение в zip
            String messageText = getTextFromMessage(message);
            String zipFileName = "message.zip";
            FileService.zipMessageText(messageText, zipFileName);

            // отправляем сообщение в топик #recycle
            // вернуть из метода ИД отправленного сообщения.
            int recycleMessageId = sendZippedMessageToRecycleTopic(
                    zipFileName, message.getChatId().toString(), message.getFrom().getUserName());
            // удаляем архив с диска
            FileService.deleteZipFromDisk(zipFileName);
            //TODO записываем в базу ИД отправленного сообщения и топик (нужна БД)
        }
        // удаляем сообщение пользователя
        deleteCurrentMessage(message);


        // в текущем топике:
        // > <@nick> ваше сообщение было удалено, потому что <причина>.
        // > Его текст временно [доступен](ссылка на сообщение в #recycle) в корзине.
        // записываем ид сообщения в базу

        // планируем удаление основного сообщение через n минут (из конфига)
        // планируем удаление сообщения из корзины через m минут (из конфига)
    }

    private boolean isSystemMessage(Message message) {
        return message.getForumTopicClosed() != null ||
                message.getForumTopicReopened() != null;
    }

    private String getTextFromMessage(Message message) {
        String textMessage;
        if (message.hasText() && message.hasCaption()) {
            textMessage = message.getText() + "\n\r\n\r" + message.getCaption();
        } else if (message.hasText()) {
            textMessage = message.getText();
        } else {
            textMessage = message.getCaption();
        }
        return textMessage;
    }

    private void deleteCurrentMessage(Message message) {
        DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
        try {
            botClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private int sendZippedMessageToRecycleTopic(String zipFileName, String chatId, String senderUsername) {
        // > <@nick> <#topic> <HH:MM:SS> с вложением архива
        InputFile file = new InputFile(Path.of(zipFileName).toFile());
        SendDocument sendDocument = new SendDocument(chatId, file);
        //TODO название топика брать из конфига
        //TODO форматирование времени перенести в DateTimeService
        sendDocument.setCaption("@" + senderUsername + " topic " +
                ZonedDateTime.now(ZoneId.of("+3")).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        //TODO ид топика-корзины брать из конфига.
        sendDocument.setMessageThreadId(98);
        try {
            Message sentMessage = botClient.execute(sendDocument);
            return sentMessage.getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
