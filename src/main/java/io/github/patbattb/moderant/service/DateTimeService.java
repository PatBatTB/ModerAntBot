package io.github.patbattb.moderant.service;

import io.github.patbattb.moderant.database.UserMutingService;
import io.github.patbattb.moderant.domain.ForumTopic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class DateTimeService {

    private static final Logger LOG = LogManager.getLogger(DateTimeService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("+3");

     public static boolean verifyMutingTime(long userId, Integer topicId, int messageDate) {
         UserMutingService dbService = new UserMutingService();
         Optional<Integer> unmuteTimeOptional;
         try {
             unmuteTimeOptional = dbService.getUnmuteTime(userId, topicId);
         } catch (SQLException e) {
             LOG.error("Unmute time getting from DB failed.");
             throw new RuntimeException(e);
         }
         if (unmuteTimeOptional.isEmpty()) {
             return true;
         }
         return messageDate >= unmuteTimeOptional.get();
     }

     public static void recordMutingTime(long userId, ForumTopic topic, int messageDate) {
        UserMutingService dbService = new UserMutingService();
        int unmuteTime = calculateUnmuteTime(topic, messageDate);
         try {
             dbService.update(userId, topic.getId(), unmuteTime);
         } catch (SQLException e) {
             LOG.error("Unmute time updating failed.");
             throw new RuntimeException(e);
         }
     }

    public static String getCurrentMskTime() {
        return ZonedDateTime.now(ZONE_ID).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private static int calculateUnmuteTime(ForumTopic topic, int messageDate) {
        return (topic.getMutingMinutes() == null) ?
                getRestrictUntilUnixTime(messageDate) :
                messageDate + (topic.getMutingMinutes() * 60);
    }

    private static int getRestrictUntilUnixTime(int messageDate) {
        Instant instant = Instant.ofEpochSecond(messageDate);
        ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZONE_ID);
        time = time.truncatedTo(ChronoUnit.DAYS).plusDays(1);
        long epochSecond = time.toInstant().getEpochSecond();
        epochSecond += 31;
        return (int) epochSecond;
    }
}
