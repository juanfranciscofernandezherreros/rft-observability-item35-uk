package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Data;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Items;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.TimeSeries;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants.*;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_yyyy_MM_dd;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_yyyy_MM_dd_hh_mm_ss;

public class JsonUtils {

    public static List<Capacity> extractCapacityFromJson(String jsonContent) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Capacity> listCapacityForDay = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            if (CollectionUtils.isNotEmpty(rootNode.findParents(ITEMS))) {
                JsonNode itemsNode = rootNode.path(ITEMS);
                for (JsonNode itemNode : itemsNode) {

                    JsonNode timeSeriesNode = itemNode.get(TIME_SERIES);
                    if (timeSeriesNode.isArray()) {
                        for (JsonNode entryNode : timeSeriesNode) {
                            JsonNode dataNode = entryNode.get(DATA);
                            if (dataNode.isArray()) {
                                for (JsonNode dataEntryNode : dataNode) {
                                    String date = LocalDateTime.parse(dataEntryNode.path(TIMESTAMP).asText(), DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd_hh_mm_ss))
                                        .format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd));
                                    String minValue = dataEntryNode.path(AGGREGATE_STATISTICS).path(MIN).asText();
                                    String maxValue = dataEntryNode.path(AGGREGATE_STATISTICS).path(MAX).asText();
                                    String mean = dataEntryNode.path(AGGREGATE_STATISTICS).path(MEAN).asText();
                                    listCapacityForDay.add(new Capacity(date, maxValue, minValue, mean, StringUtils.EMPTY));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            RftLog.error(e.getMessage(), "");
        }
        return listCapacityForDay;
    }

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
