package io.github.patbattb.moderant.database;

public class UserMuting {
    private final long userId;
    private final int topicId;
    private Integer unmuteTime;

    public UserMuting (long userId, int topicId, Integer unmuteTime) {
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

    public Integer getUnmuteTime() {
        return unmuteTime;
    }

    public void setUnmuteTime(Integer unmuteTime) {
        this.unmuteTime = unmuteTime;
    }
}
