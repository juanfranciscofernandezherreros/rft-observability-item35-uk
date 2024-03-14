package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Data;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Items;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.TimeSeries;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_yyyy_MM_dd;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_yyyy_MM_dd_hh_mm_ss;

public class CapacityMapper {

    public static List<Capacity> mapperResponseToListCapacity(Response response) {
        List<Capacity> listCapacityForDay = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(response.getItems())) {
            for (Items items : response.getItems()) {
                if (CollectionUtils.isNotEmpty(items.getTimeSeries())) {
                    for (TimeSeries timeSeriesNode : items.getTimeSeries()) {
                        if (CollectionUtils.isNotEmpty(timeSeriesNode.getData())) {
                            for (Data data : timeSeriesNode.getData()) {
                                String date = LocalDateTime.parse(data.getTimestamp(), DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd_hh_mm_ss))
                                    .format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd));
                                String minValue = data.getAggregateStatistics().getMin().toString();
                                String maxValue = data.getAggregateStatistics().getMax().toString();
                                String mean = data.getAggregateStatistics().getMean().toString();
                                listCapacityForDay.add(new Capacity(date, maxValue, minValue, mean, StringUtils.EMPTY));
                            }
                        }
                    }
                }
            }
        }
        return listCapacityForDay;
    }

}
