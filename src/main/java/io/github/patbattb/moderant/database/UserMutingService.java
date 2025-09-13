package io.github.patbattb.moderant.database;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

public class UserMutingService {

    private final UserMutingDAO dao = new UserMutingDAO();

    public Optional<Instant> getUnmuteTime(long userId, int topicId) throws SQLException {
        Optional<UserMuting> userMutingOptional = dao.get(userId, topicId);
        return userMutingOptional.map(UserMuting::getUnmuteTime);
    }

    public boolean update (long userId, int topicId, Instant unmuteTime) throws SQLException {
        Optional<UserMuting> userMutingOptional = dao.get(userId, topicId);
        if (userMutingOptional.isPresent()) {
            UserMuting userMuting = userMutingOptional.get();
            return dao.update(userMuting.getUserId(), userMuting.getTopicId(), unmuteTime);
        } else {
            return dao.insert(userId, topicId, unmuteTime);
        }
    }
}
