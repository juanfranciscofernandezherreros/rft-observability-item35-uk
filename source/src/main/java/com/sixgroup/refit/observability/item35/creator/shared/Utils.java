package com.sixgroup.refit.observability.item35.creator.shared;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utils {
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd);
    public static String getFirstDayOfPreviousMonth(LocalDate currentDate) {
        LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
        firstDayOfMonth = firstDayOfMonth.minusMonths(1);
        return firstDayOfMonth.format(dateFormatter);
    }

    public static String getLastDayOfPreviousMonth(LocalDate currentDate) {
        LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
        LocalDate lastDayOfPreviousMonth = firstDayOfMonth.minusDays(1);
        return lastDayOfPreviousMonth.format(dateFormatter);
    }

}
