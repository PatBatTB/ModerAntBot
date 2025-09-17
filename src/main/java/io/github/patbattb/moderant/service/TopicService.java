package io.github.patbattb.moderant.service;

import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.domain.VerifyResult;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class TopicService {

    private static final VerifyResult approvedResult = new VerifyResult(true, "");

    public static VerifyResult verifyPermissions(ForumTopic topic, Message message) {
        VerifyResult result = approvedResult;
        if (message.hasAnimation() && result.isApproved()) {
            result = verifyAnimation(topic);
        }
        if (message.hasAudio() && result.isApproved()) {
            result = verifyAudio(topic);
        }
        if (message.hasContact() && result.isApproved()) {
            result = verifyContact(topic);
        }
        if (message.hasDocument() && result.isApproved()) {
            result = verifyDocument(topic);
        }
        if (message.hasLocation() && result.isApproved()) {
            result = verifyLocation(topic);
        }
        if (message.hasText() && result.isApproved()) {
            result = verifyText(topic);
        }
        if (message.hasPhoto() && result.isApproved()) {
            result = verifyPhoto(topic);
        }
        if (message.hasSticker() && result.isApproved()) {
            result = verifySticker(topic);
        }
        if (message.hasVideo() && result.isApproved()) {
            result = verifyVideo(topic);
        }
        return result;
    }

    private static VerifyResult verifyAnimation(ForumTopic topic) {
        return !topic.getPermissions().isAnimation() ?
                new VerifyResult(false, "Запрещена анимация в сообщениях.") :
                approvedResult;
    }

    private static VerifyResult verifyAudio(ForumTopic topic) {
        return !topic.getPermissions().isAudio() ?
                new VerifyResult(false, "Запрещено аудио.") :
                approvedResult;
    }

    private static VerifyResult verifyContact(ForumTopic topic) {
        return !topic.getPermissions().isContact() ?
                new VerifyResult(false, "Запрещена отправка контактов.") :
                approvedResult;
    }

    private static VerifyResult verifyDocument(ForumTopic topic) {
        return !topic.getPermissions().isDocument() ?
                new VerifyResult(false, "Запрещены видео.") :
                approvedResult;
    }

    private static VerifyResult verifyLocation(ForumTopic topic) {
        return !topic.getPermissions().isLocation() ?
                new VerifyResult(false, "Запрещена передача геолокации.") :
                approvedResult;
    }

    private static VerifyResult verifyText(ForumTopic topic) {
        return !topic.getPermissions().isText() ?
                new VerifyResult(false, "Запрещены текстовые сообщения.") :
                approvedResult;
    }

    private static VerifyResult verifyPhoto(ForumTopic topic) {
        return !topic.getPermissions().isPhoto() ?
                new VerifyResult(false, "Запрещены фото.") :
                approvedResult;
    }

    private static VerifyResult verifySticker(ForumTopic topic) {
        return !topic.getPermissions().isSticker() ?
                new VerifyResult(false, "Запрещены стикеры.") :
                approvedResult;
    }

    private static VerifyResult verifyVideo(ForumTopic topic) {
        return !topic.getPermissions().isVideo() ?
                new VerifyResult(false, "Запрещены видео.") :
                approvedResult;
    }
}
