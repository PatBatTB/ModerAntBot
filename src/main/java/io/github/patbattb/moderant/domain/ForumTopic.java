package io.github.patbattb.moderant.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.patbattb.moderant.domain.serialize.ForumTopicDeserializer;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@JsonDeserialize(using = ForumTopicDeserializer.class)
public class ForumTopic {

    private final Integer id;
    private final String title;
    private final TopicPermissions permissions;

    public ForumTopic(Integer id, String title, TopicPermissions permissions) {
        this.id = id;
        this.title = title;
        this.permissions = permissions;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean verifyPermissions(Message message) {
        return (permissions.isAnimation() || !message.hasAnimation()) &&
                (permissions.isAudio() || !message.hasAudio()) &&
                (permissions.isContact() || !message.hasContact()) &&
                (permissions.isDocument() || !message.hasDocument()) &&
                (permissions.isLocation() || !message.hasLocation()) &&
                (permissions.isText() || !message.hasText()) &&
                (permissions.isPhoto() || !message.hasPhoto()) &&
                (permissions.isSticker() || !message.hasSticker()) &&
                (permissions.isVideo() || !message.hasVideo());
    }
}
