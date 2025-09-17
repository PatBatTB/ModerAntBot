package io.github.patbattb.moderant;

import io.github.patbattb.moderant.database.MessageDeleteService;
import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.domain.VerifyResult;
import io.github.patbattb.moderant.service.DateTimeService;
import io.github.patbattb.moderant.service.FileService;
import io.github.patbattb.moderant.service.MessageService;
import io.github.patbattb.moderant.service.TopicService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Path;

public class UpdateHandler {

    private final BotSync botClient;
    private final Logger log = LogManager.getLogger(UpdateHandler.class);
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
            log.error(e);
        }
    }

    private void handleGroupMessage(Message message) {
        Integer threadId = message.getMessageThreadId() == null ? 1 : message.getMessageThreadId();
        if (isSystemMessage(message) || threadId.equals(Parameters.getRecycleTopicId())) {
            deleteCurrentMessage(message);
            return;
        }

        ForumTopic topic = Parameters.getTopics().get(threadId);
        if (topic == null) {
            handleRestrictedMessage(message, topic, "Запрещены любые сообщения.");
            return;
        }

        VerifyResult verifyResult = TopicService.verifyPermissions(topic, message);
        if (!verifyResult.isApproved()) {
            handleRestrictedMessage(message, topic, verifyResult.cause());
             return;
        }

        verifyResult = DateTimeService.verifyMutingTime(message.getFrom().getId(), topic.getId(), message.getDate());
        if (!verifyResult.isApproved())
        {
            handleRestrictedMessage(message, topic, verifyResult.cause());
            return;
        }

        //TODO проверить ограничения по смайликам

        DateTimeService.recordMutingTime(message.getFrom().getId(), topic, message.getDate());

    }

    private void handleRestrictedMessage(Message message, ForumTopic topic, String cause) {
        if (!Parameters.getRecycleTopicId().equals(message.getMessageThreadId())) {
            Message recycleMessage = null;
            if (message.hasText() || message.hasCaption()) {
                String topicTitle = (topic == null) ? "unknown": topic.getTitle();
                recycleMessage = recyclingMessage(message, topicTitle);
                recordDeleteMessageTime(recycleMessage, Parameters.getDeleteRecycleMinutes());
            }
            Integer recycleMessageId = (recycleMessage == null) ? null : recycleMessage.getMessageId();
            Message notificationMessage = sendRecyclingNotification(message, recycleMessageId, cause);
            recordDeleteMessageTime(notificationMessage, Parameters.getDeleteTopicMinutes());
        }

        deleteCurrentMessage(message);
    }

    private boolean isSystemMessage(Message message) {
        return message.getForumTopicClosed() != null ||
                message.getForumTopicReopened() != null;
    }

    private Message recyclingMessage(Message message, String topicTitle) {
        String messageText = getTextFromMessage(message);
        FileService.zipMessageText(messageText);
        Message recycleMessage = sendZippedMessageToRecycleTopic(
                message.getChatId().toString(), message.getFrom().getUserName(), topicTitle);
        FileService.deleteZipFromDisk();
        return recycleMessage;
    }

    private Message sendRecyclingNotification(Message message, Integer recycleMessageId, String cause) {
        Message notificationMessage = null;
        String answerText = "@" + message.getFrom().getUserName() +
                " Ваше сообщение было удалено из-за нарушения правил топика:";
        if (cause != null && !cause.isBlank()) {
            answerText += "\n\"" + cause + "\"";
        }
        if (recycleMessageId != null) {
            String messageLink = MessageService.getMessageLink(message, Parameters.getRecycleTopicId(), recycleMessageId);
            answerText += "\nТекст сообщения временно [доступен в корзине](" + messageLink + ").";
        }
        answerText = MessageService.escapingString(answerText);
        SendMessage sendMessage = new SendMessage(
                message.getChatId().toString(), answerText
        );
        sendMessage.enableMarkdownV2(true);
        sendMessage.setMessageThreadId(message.getMessageThreadId());
        try {
            notificationMessage = botClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e);
        }
        return notificationMessage;
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
            log.error(e);
        }
    }

    private Message sendZippedMessageToRecycleTopic(String chatId, String senderUsername, String topicTitle) {
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
            log.error(e);
        }
        return sentMessage;
    }

    private void recordDeleteMessageTime(Message message, int deleteMinutes) {
        try {
            if (message == null) {
                throw new NullPointerException();
            }
            MessageDeleteService dbService = new MessageDeleteService();
            int deleteTime = message.getDate() + (deleteMinutes * 60);
            dbService.insert(message.getMessageId(), message.getChatId(), deleteTime);
        } catch (Exception e) {
            log.error(e);
        }
    }

}
