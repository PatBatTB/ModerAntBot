package io.github.patbattb.moderant;

import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.domain.TopicPermissions;
import io.github.patbattb.moderant.service.DateTimeService;
import io.github.patbattb.moderant.service.FileService;
import io.github.patbattb.moderant.service.MessageService;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.file.Path;
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
        ForumTopic topic = Parameters.getTopics()
                .getOrDefault(threadId, new ForumTopic(null,null, new TopicPermissions()));

            //TODO проверить ограничения по смайликам

        // если все ок - пропускаем сообщение
        // если нет:
        if (!topic.verifyPermissions(message)) {
            handleRestrictedMessage(message, topic);
        }
    }

    private void handleRestrictedMessage(Message message, ForumTopic topic) {
        // если есть текст или описание
        Integer recycleMessageId = null;
        Integer notificationMessageId = null;
        if (!Parameters.getRecycleTopicId().equals(message.getMessageThreadId())) {
            if (message.hasText() || message.hasCaption()) {
                // отправляем сообщение в корзину
                recycleMessageId = recyclingMessage(message, topic.getTitle());
                //TODO записываем в базу ИД отправленного сообщения и топик (нужна БД)
            }
            // отправляем пользователю оповещение об удалении сообщения.
            notificationMessageId = sendRecyclingNotification(message, recycleMessageId);
            //TODO записываем ид сообщения в базу
        }

        // удаляем сообщение пользователя
        deleteCurrentMessage(message);

        //TODO планируем удаление основного сообщение через n минут (из конфига)
        //TODO планируем удаление сообщения из корзины через m минут (из конфига)
    }

    private boolean isSystemMessage(Message message) {
        return message.getForumTopicClosed() != null ||
                message.getForumTopicReopened() != null;
    }

    private Integer recyclingMessage(Message message, String topicTitle) {
        // архивируем сообщение в zip
        String messageText = getTextFromMessage(message);
        String zipFileName = "message.zip";
        FileService.zipMessageText(messageText, zipFileName);

        // отправляем сообщение в топик #recycle
        // вернуть из метода ИД отправленного сообщения.
        Integer recycleMessageId = sendZippedMessageToRecycleTopic(
                zipFileName, message.getChatId().toString(), message.getFrom().getUserName(), topicTitle);
        // удаляем архив с диска
        FileService.deleteZipFromDisk(zipFileName);
        return recycleMessageId;
    }

    private Integer sendRecyclingNotification(Message message, Integer recycleMessageId) {
        Integer notificationMessageId = null;
        // в текущем топике:
        // > <@nick> ваше сообщение было удалено, потому что <причина>.
        String answerText = "@" + message.getFrom().getUserName() +
                " Ваше сообщение было удалено из\\-за нарушения требований топика\\.";
        // если сообщение было отправлено в корзину - добавить:
        // > Его текст временно [доступен](ссылка на сообщение в #recycle) в корзине.
        if (recycleMessageId != null) {
            String messageLink = MessageService.getMessageLink(message, Parameters.getRecycleTopicId(), recycleMessageId);
            answerText += "\nЕго текст временно [доступен в корзине](" + messageLink + ")\\.";
        }
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), answerText);
        sendMessage.enableMarkdownV2(true);
        sendMessage.setMessageThreadId(message.getMessageThreadId());
        try {
            notificationMessageId = botClient.execute(sendMessage).getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return notificationMessageId;
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

    private Integer sendZippedMessageToRecycleTopic(String zipFileName, String chatId,
                                                String senderUsername, String topicTitle) {
        // > <@nick> <#topic> <HH:MM:SS> с вложением архива
        InputFile file = new InputFile(Path.of(zipFileName).toFile());
        SendDocument sendDocument = new SendDocument(chatId, file);
        sendDocument.setCaption("@" + senderUsername +
                " #" + topicTitle + " " +
                DateTimeService.getCurrentMskTime());
        sendDocument.setMessageThreadId(Parameters.getRecycleTopicId());
        try {
            Message sentMessage = botClient.execute(sendDocument);
            return sentMessage.getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
