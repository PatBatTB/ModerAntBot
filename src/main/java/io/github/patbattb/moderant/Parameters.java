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
    //TODO change to working path.
    private static final Path SETTINGS_FILE = Path.of("testData", "settings.json");

    //Json field's names
    private final static String TOPICS_FIELD_NAME = "topics";
    private final static String BOT_TOKEN_FIELD_NAME = "botToken";
    private final static String RECYCLE_ID_FIELD_NAME = "recycleId";
    private final static String RESTRICTION_MINUTES_FIELD_NAME = "restrictionMinutes";
    private final static String DELETE_TOPIC_MINUTES_FIELD_NAME = "deleteTopicMinutes";
    private final static String DELETE_RECYCLE_MINUTES_FIELD_NAME = "deleteRecycleMinutes";

    private static Integer recycleTopicId;
    private static String botToken;
    private static Integer defaultMutingMinutes;
    private static Integer deleteTopicMinutes = 5; //by default
    private static Integer deleteRecycleMinutes = 10; //by default

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
        JsonNode topicsNode = rootNode.get(TOPICS_FIELD_NAME);
        initBotToken(rootNode);
        initRecycleId(rootNode);
        initRestrictionTime(rootNode);
        initTopicsSettings(topicsNode);
        initDeleteMessageTime(rootNode);
    }

    private static void initBotToken(JsonNode rootNode) {
        JsonNode tokenNode = rootNode.get(BOT_TOKEN_FIELD_NAME);
        if (tokenNode == null) {
            throw new RuntimeException("You need to specify text object '"+BOT_TOKEN_FIELD_NAME+"' in the config file.");
        }
        String token = tokenNode.asText();
        if (token.isBlank()) {
            throw new RuntimeException("object '"+BOT_TOKEN_FIELD_NAME+"' in the config file can't be blank");
        }
        botToken = token;
    }

    private static void initRestrictionTime(JsonNode rootNode) {
        JsonNode restrictionTimeNode = rootNode.get(RESTRICTION_MINUTES_FIELD_NAME);
        if (restrictionTimeNode == null) {
            defaultMutingMinutes = null;
            return;
        }
        if (!restrictionTimeNode.isInt()) {
            throw new RuntimeException("You need to specify integer object '"+RESTRICTION_MINUTES_FIELD_NAME+"' in the config file.");
        }
        int value = restrictionTimeNode.asInt();
        if (value < 0) {
            throw new RuntimeException("The object '"+RESTRICTION_MINUTES_FIELD_NAME+"' in the config file must be positive number");
        }
        defaultMutingMinutes = value;
    }

    private static void initTopicsSettings(JsonNode topicsNode) throws JsonProcessingException {
        if (topicsNode != null) {
            JsonMapper mapper = new JsonMapper();
            for (JsonNode node : topicsNode) {
                ForumTopic topic = mapper.readValue(node.toString(), ForumTopic.class);
                TOPICS.put(topic.getId(), topic);
            }
        }
        System.out.println();
    }

    private static void initDeleteMessageTime(JsonNode rootNode) {
        JsonNode topicTimeNode = rootNode.get(DELETE_TOPIC_MINUTES_FIELD_NAME);
        if (topicTimeNode != null) {
            initTopicDeleteTime(topicTimeNode);
        }
        JsonNode recycleTimeNode = rootNode.get(DELETE_RECYCLE_MINUTES_FIELD_NAME);
        if (recycleTimeNode != null) {
            initRecycleDeleteTime(recycleTimeNode);
        }
    }

    private static void initTopicDeleteTime(JsonNode topicTimeNode) {
        if (!topicTimeNode.isInt()) {
            throw new RuntimeException("You need to specify integer object '"+DELETE_TOPIC_MINUTES_FIELD_NAME+"' in the config file.");
        }
        int value = topicTimeNode.asInt();
        if (value < 0) {
            throw new RuntimeException("The object '"+DELETE_TOPIC_MINUTES_FIELD_NAME+"' in the config file must be positive number");
        }
        deleteTopicMinutes = value;
    }

    private static void initRecycleDeleteTime(JsonNode recycleTimeNode) {
        if (!recycleTimeNode.isInt()) {
            throw new RuntimeException("You need to specify integer object '"+DELETE_TOPIC_MINUTES_FIELD_NAME+"' in the config file.");
        }
        int value = recycleTimeNode.asInt();
        if (value < 0) {
            throw new RuntimeException("The object '"+DELETE_TOPIC_MINUTES_FIELD_NAME+"' in the config file must be positive number");
        }
        deleteRecycleMinutes = value;
    }

    private static void initRecycleId(JsonNode rootNode) {
        JsonNode recycleIdNode = rootNode.get(RECYCLE_ID_FIELD_NAME);
        if (recycleIdNode == null || !recycleIdNode.isInt()) {
            throw new RuntimeException("You need to specify integer object '"+RECYCLE_ID_FIELD_NAME+"' in the config file.");
        }
        recycleTopicId = rootNode.get(RECYCLE_ID_FIELD_NAME).asInt();
    }
}
