package io.github.patbattb.moderant.domain.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.patbattb.moderant.domain.TopicPermissions;

import java.io.IOException;

public class TopicPermissionsDeserializer extends JsonDeserializer<TopicPermissions> {

    //json fields' names
    private final String all = "all";
    private final String text = "text";
    private final String audio = "audio";
    private final String document = "document";
    private final String photo = "photo";
    private final String sticker = "sticker";
    private final String video = "video";
    private final String contact = "contact";
    private final String location = "location";
    private final String animation = "animation";

    @Override
    public TopicPermissions deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = p.readValueAsTree();
        boolean all = rootNode.has(this.all) && rootNode.get(this.all).asBoolean();
        boolean text = rootNode.has(this.text) ? rootNode.get(this.text).asBoolean() : all;
        boolean audio = rootNode.has(this.audio) ? rootNode.get(this.audio).asBoolean() : all;
        boolean document = rootNode.has(this.document) ? rootNode.get(this.document).asBoolean() : all;
        boolean photo = rootNode.has(this.photo) ? rootNode.get(this.photo).asBoolean() : all;
        boolean sticker = rootNode.has(this.sticker) ? rootNode.get(this.sticker).asBoolean() : all ;
        boolean video = rootNode.has(this.video) ? rootNode.get(this.video).asBoolean() : all;
        boolean contact = rootNode.has(this.contact) ? rootNode.get(this.contact).asBoolean() : all;
        boolean location = rootNode.has(this.location) ? rootNode.get(this.location).asBoolean() : all;
        boolean animation = rootNode.has(this.animation) ? rootNode.get(this.animation).asBoolean() : all;
        return new TopicPermissions(text, audio, document, photo, sticker, video, contact, location, animation);
    }
}
