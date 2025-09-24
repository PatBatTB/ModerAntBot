package io.github.patbattb.moderant;

import io.github.patbattb.moderant.database.MessageDeleteService;
import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.domain.VerificationResult;
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
    private final Object lock = new Object();

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
            handleRestrictedMessage(message, null, "Запрещены любые сообщения.");
            return;
        }

        VerificationResult verificationResult = TopicService.verifyPermissions(topic, message);
        if (!verificationResult.isApproved()) {
            handleRestrictedMessage(message, topic, verificationResult.cause());
             return;
        }

        verificationResult = DateTimeService.verifyMutingTime(message.getFrom().getId(), topic.getId(), message.getDate());
        if (!verificationResult.isApproved())
        {
            handleRestrictedMessage(message, topic, verificationResult.cause());
            return;
        }

        //TODO проверить ограничения по смайликам

        DateTimeService.recordMutingTime(message.getFrom().getId(), topic, message.getDate());

    }

    private void handleRestrictedMessage(Message message, ForumTopic topic, String cause) {
        if (!Parameters.getRecycleTopicId().equals(message.getMessageThreadId())) {
            Message recycleMessage = null;
            if (message.hasText() || message.hasCaption()) {
                recycleMessage = recyclingMessage(message, topic);
                recordDeleteMessageTime(recycleMessage, Parameters.getDeleteRecycleMinutes());
            }
            Integer recycleMessageId = (recycleMessage == null) ? null : recycleMessage.getMessageId();
            Message notificationMessage = sendRecyclingNotification(message, recycleMessageId, cause);
            recordDeleteMessageTime(notificationMessage, Parameters.getDeleteTopicMinutes());
            deleteCurrentMessage(message);
        }
    }

    private boolean isSystemMessage(Message message) {
        return message.getForumTopicClosed() != null ||
                message.getForumTopicReopened() != null;
    }

    private Message recyclingMessage(Message message, ForumTopic topic) {
        synchronized (lock) {
            String messageText = getTextFromMessage(message);
            FileService.zipMessageText(messageText);
            Message recycleMessage = sendZippedMessageToRecycleTopic(
                    message, message.getFrom().getUserName(), topic
            );
            FileService.deleteZipFromDisk();
            return recycleMessage;
        }
    }

    private Message sendRecyclingNotification(Message message, Integer recycleMessageId, String cause) {
        Message notificationMessage = null;
        String answerText = "@" + message.getFrom().getUserName() +
                " Ваше сообщение было удалено из-за нарушения правил топика:";
        if (cause != null && !cause.isBlank()) {
            answerText += "\n\"" + cause + "\"";
        }
        answerText = MessageService.escapingString(answerText);
        if (recycleMessageId != null) {
            String messageLink = MessageService.getMessageLink(
                    message.getChatId().toString(), Parameters.getRecycleTopicId(), recycleMessageId
            );
            answerText += "\nТекст сообщения временно [доступен в корзине](" + messageLink + ")";
        }
        SendMessage sendMessage = new SendMessage(
                message.getChatId().toString(), answerText
        );
        sendMessage.enableMarkdownV2(true);
        sendMessage.setMessageThreadId(message.getMessageThreadId());
        sendMessage.setDisableNotification(true);
        try {
            notificationMessage = botClient.execute(sendMessage);
        } catch (TelegramApiException | InterruptedException e) {
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
        } catch (TelegramApiException | InterruptedException e) {
            log.error(e);
        }
    }

    private Message sendZippedMessageToRecycleTopic(Message message, String senderUsername, ForumTopic topic) {
        InputFile file = new InputFile(Path.of(FileService.ZIP_FILE_NAME).toFile());
        String chatId = message.getChatId().toString();
        Integer topicId;
        String topicTitle;
        if (topic == null) {
            topicId = (message.getMessageThreadId() == null) ? 1 : message.getMessageThreadId();
            topicTitle = "unknown";
        } else {
            topicId = topic.getId();
            topicTitle = topic.getTitle();
        }
        SendDocument sendDocument = new SendDocument(chatId, file);
        String escapedSenderUserName = MessageService.escapingString(senderUsername);
        String escapedTopicTitle = MessageService.escapingString(topicTitle);
        String topicLink = MessageService.getTopicLink(chatId, topicId);
        sendDocument.setCaption(
                "@" + escapedSenderUserName + " [" + escapedTopicTitle + "]" +
                        "(" + topicLink + ") " + DateTimeService.getCurrentMskTime()
        );
        sendDocument.setMessageThreadId(Parameters.getRecycleTopicId());
        sendDocument.setParseMode("MarkdownV2");
        sendDocument.setDisableNotification(true);
        Message sentMessage = null;
        try {
            sentMessage = botClient.execute(sendDocument);
        } catch (TelegramApiException | InterruptedException e) {
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
