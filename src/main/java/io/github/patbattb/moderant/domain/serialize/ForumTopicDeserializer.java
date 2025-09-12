package io.github.patbattb.moderant.domain.serialize;

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

    //json field's names
    private final String id = "id";
    private final String title = "title";
    private final String permissions = "permissions";
    private final String mutingMinutes = "mutingMinutes";

    @Override
    public ForumTopic deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
         JsonNode rootNode = p.readValueAsTree();
        JsonNode idNode = rootNode.get(this.id);
        Integer idValue = idNode instanceof NullNode ? null : rootNode.get(this.id).asInt();
        String titleValue = rootNode.get(this.title).asText();
        JsonNode mutingMinutesNode = rootNode.get(mutingMinutes);
        Integer mutingMinutesValue = mutingMinutesNode == null ?
                null :
                rootNode.get(this.mutingMinutes).asInt();
        JsonNode permissionsNode = rootNode.get(this.permissions);
        TopicPermissions permissions = permissionsNode != null ?
                new JsonMapper().readValue(permissionsNode.toString(), TopicPermissions.class) :
                new TopicPermissions();
        return new ForumTopic(idValue, titleValue, mutingMinutesValue, permissions);
    }
}
