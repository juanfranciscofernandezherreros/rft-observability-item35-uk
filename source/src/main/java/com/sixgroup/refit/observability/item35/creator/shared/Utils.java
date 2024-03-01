package com.sixgroup.refit.observability.item35.creator.shared;

import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static com.sixgroup.refit.observability.item35.creator.shared.Constants.DATE_FORMAT_yyyy_MM_dd;

public class Utils {
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd);

    public static String getFileName(String itemDate) {
        return ItemType.SUBMISSION_VOLUMES.getNamePattern() + itemDate + ".csv";
    }

    public static String getFirstDayOfMonthAndYear(String itemDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date = sdf.parse(itemDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getLastDayOfMonthAndYear(String itemDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date = sdf.parse(itemDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, lastDay);
            return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}
