package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Slf4j
public final class DateUtils {
    private static final DateTimeFormatter DATE_FORMAT_YYYYMMDD = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_YYYYMMDD);
    private static final DateTimeFormatter DATE_FORMAT_YYYY_MM_DD = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_YYYY_MM_DD);
    private static final DateTimeFormatter DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);

    private DateUtils() {
    }

    public static String firstDayOfPreviousMonth(final String itemDate) {
        final LocalDate date = LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD);
        final LocalDate initialDate = date.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        return initialDate.format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String firstDayOfCurrentMonth(final String itemDate) {
        final LocalDate date = LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD);
        final LocalDate dateFirstDayOfCurrentMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        return dateFirstDayOfCurrentMonth.format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String lastDayOfPreviousMonth(final String itemDate) {
        final LocalDate date = LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD);
        final LocalDate lastDayOfMonth = date.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return lastDayOfMonth.format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String reportingReferenceDate(final String itemDate) {
        final LocalDate date = LocalDate.parse(itemDate, DATE_FORMAT_YYYYMMDD);
        return date.minusMonths(1).withDayOfMonth(15).format(DATE_FORMAT_YYYYMMDD);
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
        return localDateTime.format(DATE_FORMAT_YYYY_MM_DD);
    }

    public static String localDateToString(final LocalDate localDate){
        return localDate.format(DATE_FORMAT_YYYY_MM_DD);

    }
}
