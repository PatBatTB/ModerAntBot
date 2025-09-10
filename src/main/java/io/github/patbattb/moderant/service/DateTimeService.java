package io.github.patbattb.moderant.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeService {

    public static int getRestrictUntilUnixTime(int messageDate) {
        Instant instant = Instant.ofEpochSecond(messageDate);
        ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZoneId.of("+3"));
        time = time.truncatedTo(ChronoUnit.DAYS).plusDays(1);
        long epochSecond = time.toInstant().getEpochSecond();
        epochSecond += 31;
        return (int) epochSecond;
    }

    public static String getCurrentMskTime() {
        return ZonedDateTime.now(ZoneId.of("+3")).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
