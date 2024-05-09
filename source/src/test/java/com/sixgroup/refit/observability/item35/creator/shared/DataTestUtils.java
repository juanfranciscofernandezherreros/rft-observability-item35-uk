package com.sixgroup.refit.observability.item35.creator.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DataTestUtils {

    public static final OffsetDateTime ORIGIN_DATE = OffsetDateTime
        .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    public static LocalDateTime parseString(final String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String calculateDifference(final LocalDateTime reportingSession, final LocalDateTime endDate) {
        OffsetDateTime endDateOffsetDateTime = endDate.truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = reportingSession.truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(6);

        if (endDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = reportingSession.atOffset(ZoneOffset.UTC).plusDays(1);
            // REPORT_PUBLICATION_TIME (NOW IS USING endDate) - SLA
            // The format is a float that indicates the difference time between this two dates.
            float seconds = Duration.between(endDate.atOffset(ZoneOffset.UTC),
                slaDateOffsetDateTime).getSeconds() / 3600.00f;
            return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN).toString();
        }
        return "";
    }
}
