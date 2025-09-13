package io.github.patbattb.moderant;

import io.github.patbattb.moderant.database.UserMutingService;
import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.service.DateTimeService;
import io.github.patbattb.moderant.service.FileService;
import io.github.patbattb.moderant.service.MessageService;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;

public class UpdateHandler {

    private final BotSync botClient;


    private final String supergroupType = "supergroup";

    public UpdateHandler(BotSync botClient) {
        this.botClient = botClient;
    }

    public void handle(Update update) {
        try {
            if (update.hasMessage() && supergroupType.equals(update.getMessage().getChat().getType())) {
                handleGroupMessage(update.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGroupMessage(Message message) {
        if (isSystemMessage(message)) {
            deleteCurrentMessage(message);
            return;
        }

        Integer threadId = message.getMessageThreadId();
        //TODO debug записывает время получения сообщения вместо мьюта
        UserMutingService umService = new UserMutingService();
        try {
            umService.update(message.getFrom().getId(), message.getMessageThreadId(), Instant.now());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //TODO сравнить ограничение пользователя по времени с разрешениями в данном топике (Нужна БД)
        ForumTopic topic = Parameters.getTopics().getOrDefault(threadId, ForumTopic.getDefault());

        //TODO проверить ограничения по смайликам

        if (!topic.verifyPermissions(message)) {
            handleRestrictedMessage(message, topic);
        }
    }

    private void handleRestrictedMessage(Message message, ForumTopic topic) {
        Integer recycleMessageId = null;
        Integer notificationMessageId = null;
        if (!Parameters.getRecycleTopicId().equals(message.getMessageThreadId())) {
            if (message.hasText() || message.hasCaption()) {
                recycleMessageId = recyclingMessage(message, topic.getTitle());
                //TODO записываем в базу ИД отправленного сообщения и топик (нужна БД)
            }
            notificationMessageId = sendRecyclingNotification(message, recycleMessageId);
            //TODO записываем ид сообщения в базу
        }

        deleteCurrentMessage(message);

        //TODO планируем удаление основного сообщение через n минут (из конфига)
        //TODO планируем удаление сообщения из корзины через m минут (из конфига)
    }

    private boolean isSystemMessage(Message message) {
        return message.getForumTopicClosed() != null ||
                message.getForumTopicReopened() != null;
    }

    private Integer recyclingMessage(Message message, String topicTitle) {
        String messageText = getTextFromMessage(message);
        FileService.zipMessageText(messageText);
        Integer recycleMessageId = sendZippedMessageToRecycleTopic(
                message.getChatId().toString(), message.getFrom().getUserName(), topicTitle);
        FileService.deleteZipFromDisk();
        return recycleMessageId;
    }

    private Integer sendRecyclingNotification(Message message, Integer recycleMessageId) {
        Integer notificationMessageId = null;
        String answerText = "@" + message.getFrom().getUserName() +
                " Ваше сообщение было удалено из\\-за нарушения требований топика\\.";
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private Integer sendZippedMessageToRecycleTopic(String chatId, String senderUsername, String topicTitle) {
        InputFile file = new InputFile(Path.of(FileService.ZIP_FILE_NAME).toFile());
        SendDocument sendDocument = new SendDocument(chatId, file);
        sendDocument.setCaption("@" + senderUsername +
                " #" + topicTitle + " " +
                DateTimeService.getCurrentMskTime());
        sendDocument.setMessageThreadId(Parameters.getRecycleTopicId());
        Message sentMessage = null;
        try {
            sentMessage = botClient.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return sentMessage.getMessageId();
    }
}
