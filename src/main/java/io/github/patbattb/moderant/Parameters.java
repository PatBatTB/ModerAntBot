package io.github.patbattb.moderant;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.patbattb.moderant.domain.ForumTopic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

public class Parameters {
    private static final HashMap<Integer, ForumTopic> TOPICS = new HashMap<>();
    private static final Path SETTINGS_FILE = Path.of("testData", "settings.json");

    private static Integer recycleTopicId;

    private static String botToken;

    public static HashMap<Integer, ForumTopic> getTopics() {
        return TOPICS;
    }

    public static Integer getRecycleTopicId() {
        return recycleTopicId;
    }

    public static String getBotToken() {
        return botToken;
    }

    public static void init() {
        try {
            readSettings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readSettings() throws IOException {
        JsonMapper mapper = new JsonMapper();
        JsonNode rootNode = mapper.readTree(SETTINGS_FILE.toFile());
        JsonNode topicsNode = rootNode.get("topics");
        readBotToken(rootNode);
        readRecycleId(rootNode);
        readTopicsSettings(topicsNode);
    }

    private static void readBotToken(JsonNode rootNode) {
        JsonNode tokenNode = rootNode.get("botToken");
        if (tokenNode == null) {
            throw new RuntimeException("You need to specify text object 'botToken' in the config file.");
        }
        String token = tokenNode.asText();
        if (token.isBlank()) {
            throw new RuntimeException("object 'botToken' in the config file can't be blank");
        }
        botToken = token;
    }

    private static void readTopicsSettings(JsonNode topicsNode) throws JsonProcessingException {
        if (topicsNode != null) {
            JsonMapper mapper = new JsonMapper();
            for (JsonNode node : topicsNode) {
                ForumTopic topic = mapper.readValue(node.toString(), ForumTopic.class);
                TOPICS.put(topic.getId(), topic);
            }
        }
    }

    private static void readRecycleId(JsonNode rootNode) {
        JsonNode recycleIdNode = rootNode.get("recycleId");
        if (recycleIdNode == null || !recycleIdNode.isInt()) {
            throw new RuntimeException("You need to specify integer object 'recycleId' in the config file.");
        }
        recycleTopicId = rootNode.get("recycleId").asInt();
    }
}
