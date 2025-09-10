package io.github.patbattb.moderant.domain.serialize;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.patbattb.moderant.domain.TopicPermissions;

import java.io.IOException;

public class TopicPermissionsDeserializer extends JsonDeserializer<TopicPermissions> {
    @Override
    public TopicPermissions deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = p.readValueAsTree();
        boolean all = rootNode.has("all") && rootNode.get("all").asBoolean();
        boolean text = rootNode.has("text") ? rootNode.get("text").asBoolean() : all;
        boolean audio = rootNode.has("audio") ? rootNode.get("audio").asBoolean() : all;
        boolean document = rootNode.has("document") ? rootNode.get("document").asBoolean() : all;
        boolean photo = rootNode.has("photo") ? rootNode.get("photo").asBoolean() : all;
        boolean sticker = rootNode.has("sticker") ? rootNode.get("sticker").asBoolean() : all ;
        boolean video = rootNode.has("video") ? rootNode.get("video").asBoolean() : all;
        boolean contact = rootNode.has("contact") ? rootNode.get("contact").asBoolean() : all;
        boolean location = rootNode.has("location") ? rootNode.get("location").asBoolean() : all;
        boolean animation = rootNode.has("animation") ? rootNode.get("animation").asBoolean() : all;
        return new TopicPermissions(text, audio, document, photo, sticker, video, contact, location, animation);
    }
}
