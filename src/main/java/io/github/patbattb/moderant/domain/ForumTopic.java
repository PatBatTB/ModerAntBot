package io.github.patbattb.moderant.domain;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public class ForumTopic {

    private final Integer id;
    private boolean isTextRestricted;
    private boolean isAudioRestricted;
    private boolean isDocumentRestricted;
    private boolean isPhotoRestricted;
    private boolean isStickerRestricted;
    private boolean isVideoRestricted;
    private boolean isContactRestricted;
    private boolean isLocationRestricted;
    private boolean isAnimationRestricted;

    public ForumTopic(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public boolean isTextRestricted() {
        return isTextRestricted;
    }

    public void setTextRestricted(boolean textRestricted) {
        isTextRestricted = textRestricted;
    }

    public boolean isAudioRestricted() {
        return isAudioRestricted;
    }

    public void setAudioRestricted(boolean audioRestricted) {
        isAudioRestricted = audioRestricted;
    }

    public boolean isDocumentRestricted() {
        return isDocumentRestricted;
    }

    public void setDocumentRestricted(boolean documentRestricted) {
        isDocumentRestricted = documentRestricted;
    }

    public boolean isPhotoRestricted() {
        return isPhotoRestricted;
    }

    public void setPhotoRestricted(boolean photoRestricted) {
        isPhotoRestricted = photoRestricted;
    }

    public boolean isStickerRestricted() {
        return isStickerRestricted;
    }

    public void setStickerRestricted(boolean stickerRestricted) {
        isStickerRestricted = stickerRestricted;
    }

    public boolean isVideoRestricted() {
        return isVideoRestricted;
    }

    public void setVideoRestricted(boolean videoRestricted) {
        isVideoRestricted = videoRestricted;
    }

    public boolean isContactRestricted() {
        return isContactRestricted;
    }

    public void setContactRestricted(boolean contactRestricted) {
        isContactRestricted = contactRestricted;
    }

    public boolean isLocationRestricted() {
        return isLocationRestricted;
    }

    public void setLocationRestricted(boolean locationRestricted) {
        isLocationRestricted = locationRestricted;
    }

    public boolean isAnimationRestricted() {
        return isAnimationRestricted;
    }

    public void setAnimationRestricted(boolean animationRestricted) {
        isAnimationRestricted = animationRestricted;
    }

    public boolean checkForRestrictions(Message message) {
        return this.isAnimationRestricted() && message.hasAnimation() ||
                this.isAudioRestricted() && message.hasAudio() ||
                this.isContactRestricted() && message.hasContact() ||
                this.isDocumentRestricted() && message.hasDocument() ||
                this.isLocationRestricted() && message.hasLocation() ||
                this.isTextRestricted() && message.hasText() ||
                this.isPhotoRestricted() && message.hasPhoto() ||
                this.isStickerRestricted() && message.hasSticker() ||
                this.isVideoRestricted() && message.hasVideo();
    }
}
