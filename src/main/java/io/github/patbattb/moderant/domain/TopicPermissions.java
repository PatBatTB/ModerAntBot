package io.github.patbattb.moderant.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.patbattb.moderant.domain.serialize.TopicPermissionsDeserializer;

@JsonDeserialize(using = TopicPermissionsDeserializer.class)
public class TopicPermissions {

    private boolean text;
    private boolean audio;
    private boolean document;
    private boolean photo;
    private boolean sticker;
    private boolean video;
    private boolean contact;
    private boolean location;
    private boolean animation;

    public TopicPermissions() {}

    public TopicPermissions(boolean text, boolean audio, boolean document, boolean photo, boolean sticker,
                            boolean video, boolean contact, boolean location, boolean animation) {
        this.text = text;
        this.audio = audio;
        this.document = document;
        this.photo = photo;
        this.sticker = sticker;
        this.video = video;
        this.contact = contact;
        this.location = location;
        this.animation = animation;
    }

    public boolean isAnimation() {
        return animation;
    }

    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    public boolean isLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public boolean isContact() {
        return contact;
    }

    public void setContact(boolean contact) {
        this.contact = contact;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public boolean isSticker() {
        return sticker;
    }

    public void setSticker(boolean sticker) {
        this.sticker = sticker;
    }

    public boolean isPhoto() {
        return photo;
    }

    public void setPhoto(boolean photo) {
        this.photo = photo;
    }

    public boolean isDocument() {
        return document;
    }

    public void setDocument(boolean document) {
        this.document = document;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public boolean isText() {
        return text;
    }

    public void setText(boolean text) {
        this.text = text;
    }
}
