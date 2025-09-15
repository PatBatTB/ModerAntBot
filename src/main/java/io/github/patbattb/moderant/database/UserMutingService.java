package io.github.patbattb.moderant.database;

import java.sql.SQLException;
import java.util.Optional;

public class UserMutingService {

    private final UserMutingDAO dao = new UserMutingDAO();

    public Optional<Integer> getUnmuteTime(long userId, Integer topicId) throws SQLException {
        Optional<UserMuting> userMutingOptional = dao.get(userId, topicId);
        return userMutingOptional.map(UserMuting::getUnmuteTime);
    }

    public boolean update (long userId, Integer topicId, int unmuteTime) throws SQLException {
        Optional<UserMuting> userMutingOptional = dao.get(userId, topicId);
        if (userMutingOptional.isPresent()) {
            UserMuting userMuting = userMutingOptional.get();
            return dao.update(userMuting.getUserId(), userMuting.getTopicId(), unmuteTime);
        } else {
            return dao.insert(userId, topicId, unmuteTime);
        }
    }
}
