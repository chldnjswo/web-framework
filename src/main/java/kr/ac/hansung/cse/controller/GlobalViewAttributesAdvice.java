package kr.ac.hansung.cse.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ControllerAdvice
public class GlobalViewAttributesAdvice {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter PRACTICE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E) a hh:mm:ss", Locale.KOREAN);

    @ModelAttribute("practicePerformedAt")
    public String practicePerformedAt() {
        return ZonedDateTime.now(SEOUL_ZONE_ID).format(PRACTICE_TIME_FORMATTER);
    }
}
