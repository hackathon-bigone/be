package hackathon.bigone.sunsak.global.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DisplayDateUtil {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter THIS_YEAR_FMT = DateTimeFormatter.ofPattern("M월 d일");
    private static final DateTimeFormatter OTHER_YEAR_FMT = DateTimeFormatter.ofPattern("yy년 M월 d일");

    private DisplayDateUtil() {}

    public static String toDisplay(LocalDateTime createdAt) {
        if (createdAt == null) return "";
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime created = createdAt.atZone(ZONE);

        // 오늘 & 24시간 이내
        if (created.toLocalDate().isEqual(now.toLocalDate())) {
            Duration diff = Duration.between(created, now);
            long seconds = diff.getSeconds();

            if (seconds < 60) {
                return "방금";
            }

            long minutes = seconds / 60;
            if (minutes < 60) {
                return minutes + "분 전";
            }

            long hours = minutes / 60;
            return hours + "시간 전";
        }

        // 올해
        if (created.getYear() == now.getYear()) {
            return created.format(THIS_YEAR_FMT);
        }

        // 다른 해
        return created.format(OTHER_YEAR_FMT);
    }
}
