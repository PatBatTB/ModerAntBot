package io.github.patbattb.moderant.service;

import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.domain.VerificationResult;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class TopicService {

    private static final VerificationResult approvedResult = new VerificationResult(true, "");

    public static VerificationResult verifyPermissions(ForumTopic topic, Message message) {
        VerificationResult result = approvedResult;
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

    private static VerificationResult verifyAnimation(ForumTopic topic) {
        return !topic.getPermissions().isAnimation() ?
                new VerificationResult(false, "Запрещена анимация в сообщениях.") :
                approvedResult;
    }

    private static VerificationResult verifyAudio(ForumTopic topic) {
        return !topic.getPermissions().isAudio() ?
                new VerificationResult(false, "Запрещено аудио.") :
                approvedResult;
    }

    private static VerificationResult verifyContact(ForumTopic topic) {
        return !topic.getPermissions().isContact() ?
                new VerificationResult(false, "Запрещена отправка контактов.") :
                approvedResult;
    }

    private static VerificationResult verifyDocument(ForumTopic topic) {
        return !topic.getPermissions().isDocument() ?
                new VerificationResult(false, "Запрещены видео.") :
                approvedResult;
    }

    private static VerificationResult verifyLocation(ForumTopic topic) {
        return !topic.getPermissions().isLocation() ?
                new VerificationResult(false, "Запрещена передача геолокации.") :
                approvedResult;
    }

    private static VerificationResult verifyText(ForumTopic topic) {
        return !topic.getPermissions().isText() ?
                new VerificationResult(false, "Запрещены текстовые сообщения.") :
                approvedResult;
    }

    private static VerificationResult verifyPhoto(ForumTopic topic) {
        return !topic.getPermissions().isPhoto() ?
                new VerificationResult(false, "Запрещены фото.") :
                approvedResult;
    }

    private static VerificationResult verifySticker(ForumTopic topic) {
        return !topic.getPermissions().isSticker() ?
                new VerificationResult(false, "Запрещены стикеры.") :
                approvedResult;
    }

    private static VerificationResult verifyVideo(ForumTopic topic) {
        return !topic.getPermissions().isVideo() ?
                new VerificationResult(false, "Запрещены видео.") :
                approvedResult;
    }
}
