package io.github.patbattb.moderant.domain.serialize;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.domain.TopicPermissions;

import java.io.IOException;

public class ForumTopicDeserializer extends JsonDeserializer<ForumTopic> {
    @Override
    public ForumTopic deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = p.readValueAsTree();
        JsonNode idNode = rootNode.get("id");
        Integer id = idNode instanceof NullNode ? null : rootNode.get("id").asInt();
        String title = rootNode.get("title").asText();
        JsonNode permissionsNode = rootNode.get("permissions");
        TopicPermissions permissions = permissionsNode != null ?
                new JsonMapper().readValue(permissionsNode.toString(), TopicPermissions.class) :
                new TopicPermissions();
        return new ForumTopic(id, title, permissions);
    }
}
