package io.github.patbattb.moderant;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.patbattb.moderant.domain.ForumTopic;
import io.github.patbattb.moderant.domain.Proxy;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;

public class Parameters {
    private static final HashMap<Integer, ForumTopic> TOPICS = new HashMap<>();
    private static final Path SETTINGS_FILE = Path.of("settings.json");

    //Json field's names
    private static final String TOPICS_FIELD_NAME = "topics";
    private static final String BOT_TOKEN_FIELD_NAME = "botToken";
    private static final String PROCESS_HISTORY_FIELD_NAME = "processHistory";
    private static final String RECYCLE_ID_FIELD_NAME = "recycleId";
    private static final String MUTING_MINUTES_FIELD_NAME = "mutingMinutes";
    private static final String DELETE_TOPIC_MINUTES_FIELD_NAME = "deleteTopicMinutes";
    private static final String DELETE_RECYCLE_MINUTES_FIELD_NAME = "deleteRecycleMinutes";
    private static final String LOGGER_LEVEL_FIELD_NAME = "logLevel";
    private static final String PROXY_FIELD_NAME = "proxy";
    private static final String PROXY_TYPE_FIELD_NAME = "type";
    private static final String PROXY_HOST_FIELD_NAME = "host";
    private static final String PROXY_PORT_FIELD_NAME = "port";
    private static final String PROXY_AUTH_FIELD_NAME = "auth";
    private static final String PROXY_LOGIN_FIELD_NAME = "login";
    private static final String PROXY_PASS_FIELD_NAME = "pass";

    private static Integer recycleTopicId;
    private static String botToken;
    private static boolean processHistory;

    private static Proxy proxy;

    private static Instant messageReceivingStartDate;

    private static Integer defaultMutingMinutes;

    private static Integer deleteTopicMinutes = 5; //by default
    private static Integer deleteRecycleMinutes = 10; //by default

    private static Level logLevel;

    public static HashMap<Integer, ForumTopic> getTopics() {
        return TOPICS;
    }

    public static Integer getDefaultMutingMinutes() {
        return defaultMutingMinutes;
    }

    public static Integer getDeleteTopicMinutes() {
        return deleteTopicMinutes;
    }

    public static Integer getDeleteRecycleMinutes() {
        return deleteRecycleMinutes;
    }

    public static Integer getRecycleTopicId() {
        return recycleTopicId;
    }

    public static String getBotToken() {
        return botToken;
    }

    public static Proxy getProxy() {
        return proxy;
    }

    public static Instant getMessageReceivingStartDate() {
        return messageReceivingStartDate;
    }

    public static Level getLogLevel() {
        return logLevel;
    }

    public static void setMessageReceivingStartDate(Instant messageReceivingStartDate) {
        Parameters.messageReceivingStartDate = messageReceivingStartDate;
    }

    public static boolean isProcessHistory() {
        return processHistory;
    }

    public static void init() {
        try {
            messageReceivingStartDate = Instant.now();
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
        initProcessHistory(rootNode);
        initRecycleId(rootNode);
        initRestrictionTime(rootNode);
        initTopicsSettings(topicsNode);
        initDeleteMessageTime(rootNode);
        initLogLevel(rootNode);
        initProxy(rootNode);
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

    private static void initProxy(JsonNode rootNode) throws JsonProcessingException {
        JsonNode proxyNode = rootNode.get(PROXY_FIELD_NAME);

        if (proxyNode != null) {
            validateProxyNode(proxyNode);
            JsonMapper mapper = new JsonMapper();
            proxy = mapper.readValue(proxyNode.toString(), Proxy.class);
        }
    }

    private static void validateProxyNode(JsonNode proxyNode) {
        JsonNode typeNode = proxyNode.get(PROXY_TYPE_FIELD_NAME);
        if (typeNode == null) {
            throw new RuntimeException("You need to specify text object '"+PROXY_FIELD_NAME+"."+PROXY_TYPE_FIELD_NAME+"' in the config file.");
        }
        String type = typeNode.asText();
        if (!type.equals("HTTP") && !type.equals("SOCKS")) {
            throw new RuntimeException("You need to set correct value for object '"+PROXY_FIELD_NAME+"."+PROXY_TYPE_FIELD_NAME+"' " +
                    "Available values are: HTTP, SOCKS");
        }

        JsonNode hostNode = proxyNode.get(PROXY_HOST_FIELD_NAME);
        if (hostNode == null) {
            throw new RuntimeException("You need to specify text object '"+PROXY_FIELD_NAME+"."+PROXY_HOST_FIELD_NAME+"' in the config file.");
        }
        String host = hostNode.asText();
        if (host.isBlank()) {
            throw new RuntimeException("object '"+PROXY_FIELD_NAME+"."+PROXY_HOST_FIELD_NAME+"' in the config file can't be blank");
        }

        JsonNode portNode = proxyNode.get(PROXY_PORT_FIELD_NAME);
        if (portNode == null || !portNode.isInt()) {
            throw new RuntimeException("You need to specify integer object '"+PROXY_FIELD_NAME+"."+PROXY_PORT_FIELD_NAME+"' in the config file.");
        }
        int port = portNode.asInt();
        if (port < 0 || port > 65535) {
            throw new RuntimeException("The object '"+PROXY_FIELD_NAME+"."+PROXY_PORT_FIELD_NAME+"' in the config file must be in range 0-65535");
        }

        JsonNode authNode = proxyNode.get(PROXY_AUTH_FIELD_NAME);
        if (authNode != null) {
            validateAuthNode(authNode);
        }
    }

    private static void validateAuthNode(JsonNode authNode) {
        JsonNode loginNode = authNode.get(PROXY_LOGIN_FIELD_NAME);
        if (loginNode == null) {
            throw new RuntimeException("You need to specify text object '"+
                    PROXY_FIELD_NAME+"."+PROXY_AUTH_FIELD_NAME+"."+PROXY_LOGIN_FIELD_NAME+
                    "' in the config file.");
        }
        String login = loginNode.asText();
        if (login.isBlank()) {
            throw new RuntimeException("object '"+
                    PROXY_FIELD_NAME+"."+PROXY_AUTH_FIELD_NAME+"."+PROXY_LOGIN_FIELD_NAME+
                    "' in the config file can't be blank");
        }

        JsonNode passNode = authNode.get(PROXY_PASS_FIELD_NAME);
        if (passNode == null) {
            throw new RuntimeException("You need to specify text object '"+
                    PROXY_FIELD_NAME+"."+PROXY_AUTH_FIELD_NAME+"."+PROXY_PASS_FIELD_NAME+
                    "' in the config file.");
        }
    }

    private static void initProcessHistory(JsonNode rootNode) {
        JsonNode historyNode = rootNode.get(PROCESS_HISTORY_FIELD_NAME);
        processHistory = historyNode != null && historyNode.asBoolean();
    }

    private static void initRestrictionTime(JsonNode rootNode) {
        JsonNode restrictionTimeNode = rootNode.get(MUTING_MINUTES_FIELD_NAME);
        if (restrictionTimeNode == null) {
            defaultMutingMinutes = null;
            return;
        }
        if (!restrictionTimeNode.isInt()) {
            throw new RuntimeException("You need to specify integer object '"+ MUTING_MINUTES_FIELD_NAME +"' in the config file.");
        }
        int value = restrictionTimeNode.asInt();
        if (value < 0) {
            throw new RuntimeException("The object '"+ MUTING_MINUTES_FIELD_NAME +"' in the config file must be positive number");
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

    private static void initLogLevel(JsonNode rootNode) {
        JsonNode logLevelNode = rootNode.get(LOGGER_LEVEL_FIELD_NAME);
        if (logLevelNode == null) {
            return; // logLevel has used from XML config
        }
        Level level = Level.getLevel(logLevelNode.asText());
        if (level != null) {
            logLevel = level;
        } else {
            throw new RuntimeException("You need to set correct value for object '"+ LOGGER_LEVEL_FIELD_NAME +"'. " +
                    "Available values are: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL");
        }
    }
}
