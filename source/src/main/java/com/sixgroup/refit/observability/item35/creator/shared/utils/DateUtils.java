package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.sixgroup.refit.observability.item35.creator.shared.constants.Constants;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Slf4j
public final class DateUtils {
    private static final DateTimeFormatter DATE_FORMAT_YYYYMMDD = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYYMMDD);
    private static final DateTimeFormatter DATE_FORMAT_YYYY_MM_DD = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD);
    private static final DateTimeFormatter DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
    private static final DateTimeFormatter DATE_FORMAT_DD_MM_YYYY = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DD_MM_YYYY);

    private DateUtils() {
    }

    public static String firstDayOfMonth(final String itemDate) {
        final LocalDate date = LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD);
        final LocalDate initialDate = date.with(TemporalAdjusters.firstDayOfMonth());
        return initialDate.format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String lastDayOfMonth(final String itemDate) {
        final LocalDate date = LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD);
        final LocalDate lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        return lastDayOfMonth.format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String itemDateFormatted(final String itemDate) {
        return LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD).format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String firstDayOfNextMonth(final String itemDate) {
        final LocalDate date = LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD);
        final LocalDate dateFirstDayOfNextMonth = date.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        return dateFirstDayOfNextMonth.format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String createFileDateFromTimeStamp(final String timestamp) {
        final String[] dateArray = timestamp.split("T");
        return dateArray[0];
    }

    public static String localDateTimeToIsoFormat(final LocalDateTime localDateTime) {
        return localDateTime.format(DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
    }

    public static String localDateTimeToSpainDateFormat(final LocalDateTime localDateTime) {
        return localDateTime.format(DATE_FORMAT_DD_MM_YYYY);
    }

}
