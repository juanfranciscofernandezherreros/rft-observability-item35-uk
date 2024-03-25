package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.shared.constants.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static String getFileName(ItemCommandDTO itemCommandDTO) {
        return ItemType.getItemTypeFromName(itemCommandDTO.getItemType()).getNamePattern() + itemCommandDTO.getItemDate() + ".csv";
    }

    public static String getFirstDayOfMonthAndYear(String itemDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd);
            Date date = sdf.parse(itemDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return new SimpleDateFormat(Constants.DATE_FORMAT_yyyy_MM_dd).format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getItemDateFormatted(String itemDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd);
            Date date_1 = sdf.parse(itemDate);
            return new SimpleDateFormat(Constants.DATE_FORMAT_yyyy_MM_dd).format(date_1);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getLastDayOfMonthAndYear(String itemDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd);
            Date date = sdf.parse(itemDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, lastDay);
            return new SimpleDateFormat(Constants.DATE_FORMAT_yyyy_MM_dd).format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getFirstDayOfNextMonthAndYear(String itemDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd);
            Date date = sdf.parse(itemDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, 1); // Move to the next month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return new SimpleDateFormat(Constants.DATE_FORMAT_yyyy_MM_dd).format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }


    public static String convertBytesToTeraBytes(BigDecimal bytes) {
        return bytes.divide(new BigDecimal("1024").pow(4), 3, RoundingMode.HALF_UP).toString();
    }

    public static String createFileDateFromTimeStamp(String timestamp) {
        String[] dateArray = timestamp.split("T");
        String dayString = dateArray[0];
        return dayString.replace("-", "/");
    }

}
