package io.github.patbattb.moderant;


import io.github.patbattb.moderant.domain.ForumTopic;

import java.util.HashMap;

public class Parameters {
    private static HashMap<Integer, ForumTopic> topics;

    //TODO test
    public static void init() {
        topics = new HashMap<>();

        ForumTopic main = new ForumTopic(null);
        main.setStickerRestricted(true);

        ForumTopic topic12 = new ForumTopic(98);
        topic12.setDocumentRestricted(true);

        ForumTopic topic1 = new ForumTopic(70);
        topic1.setTextRestricted(true);

        topics.put(null, main);
        topics.put(98, topic12);
        topics.put(70, topic1);
    }

    public static HashMap<Integer, ForumTopic> getTopics() {
        return topics;
    }
}
