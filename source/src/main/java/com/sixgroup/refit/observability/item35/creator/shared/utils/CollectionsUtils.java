package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public final class CollectionsUtils {
    private static final String COMPARE_FORMAT = "yyyy-MM-dd";

    private CollectionsUtils() {
    }

    public static List<ReportGenerationDto> getOrderCollectionsByDate(final List<ReportGenerationDto> joinedCollection) {

        SimpleDateFormat compareFormat = new SimpleDateFormat(COMPARE_FORMAT);

        final  List<ReportGenerationDto> modifiableList = new ArrayList<>(joinedCollection);
        // SORT DATA BY DATE
        modifiableList.sort(Comparator.comparing(generationDto -> {
            try {
                return compareFormat.parse(generationDto.getDate());
            } catch (ParseException e) {
                log.error("Error parsing data in report generation compare sort");
                throw new RuntimeException(e);
            }
        }));
        return modifiableList;
    }
}
