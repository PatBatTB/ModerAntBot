package io.github.patbattb.moderant.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.patbattb.moderant.Parameters;
import io.github.patbattb.moderant.domain.serialize.ForumTopicDeserializer;

@JsonDeserialize(using = ForumTopicDeserializer.class)
public class ForumTopic {

    private final Integer id;
    private final String title;

    private final Integer mutingMinutes;

    private final TopicPermissions permissions;

    public ForumTopic(Integer id, String title, Integer mutingMinutes, TopicPermissions permissions) {
        this.id = id;
        this.title = title;
        this.mutingMinutes = mutingMinutes;
        this.permissions = permissions;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getMutingMinutes() {
        return mutingMinutes == null ? Parameters.getDefaultMutingMinutes() : mutingMinutes;
    }

    public TopicPermissions getPermissions() {
        return permissions;
    }
}
