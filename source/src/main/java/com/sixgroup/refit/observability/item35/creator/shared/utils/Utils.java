package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.shared.constants.Constants;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.FOUR_DECIMALS;

@Slf4j
public class Utils {

    private static final String COMPARE_FORMAT = "yyyy-MM-dd";

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
        return bytes.divide(new BigDecimal("1024").pow(4), FOUR_DECIMALS, RoundingMode.HALF_UP).toString();
    }

    public static String createFileDateFromTimeStamp(String timestamp) {
        String[] dateArray = timestamp.split("T");
        String dayString = dateArray[0];
        return dayString;
    }

    public static List<ReportGenerationDto> getOrderCollectionsByDate(List<ReportGenerationDto> joinedCollection) {

        SimpleDateFormat compareFormat = new SimpleDateFormat(COMPARE_FORMAT);

        // SORT DATA BY DATE
        joinedCollection.sort(Comparator.comparing(generationDto -> {
            try {
                return compareFormat.parse(generationDto.getDate());
            } catch (ParseException e) {
                log.error("Error parsing data in report generation compare sort");
                throw new RuntimeException(e);
            }
        }));
        return joinedCollection;
    }

}
