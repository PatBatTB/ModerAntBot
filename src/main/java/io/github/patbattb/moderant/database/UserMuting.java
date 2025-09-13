package io.github.patbattb.moderant.database;

import java.time.Instant;

public class UserMuting {
    private final long userId;
    private final int topicId;
    private Instant unmuteTime;

    public UserMuting (long userId, int topicId, Instant unmuteTime) {
        this.userId = userId;
        this.topicId = topicId;
        this.unmuteTime = unmuteTime;
    }

    public long getUserId() {
        return userId;
    }

    public int getTopicId() {
        return topicId;
    }

    public Instant getUnmuteTime() {
        return unmuteTime;
    }

    public void setUnmuteTime(Instant unmuteTime) {
        this.unmuteTime = unmuteTime;
    }
}
