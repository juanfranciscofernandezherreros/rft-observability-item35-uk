package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS;
import static org.junit.jupiter.api.Assertions.*;

class CapacityMapperTest {

    @Test
    void mapper_shoudReturnListCapacities() {
        final StorageCapacityResponse request = getRequest();

        final List<Capacity> response = CapacityMapper.mapperResponseToListCapacity(request);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(2, response.size());
        assertNotNull(response.get(0));
        assertNotNull(response.get(0).getDate());
        assertNotNull(response.get(0).getMax());
        assertNotNull(response.get(0).getMin());
        assertNotNull(response.get(0).getMean());
        assertNotNull(response.get(0).getTypeCapacity());
    }

    @Test
    void mapper_shoudReturnEmptyListWhenItemsAreEmpty() {
        final StorageCapacityResponse request = getRequest();
        request.setItems(new ArrayList<>());

        final List<Capacity> response = CapacityMapper.mapperResponseToListCapacity(request);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void mapper_shoudReturnEmptyListWhenTimeSeriesAreEmpty() {
        final StorageCapacityResponse request = getRequest();
        request.getItems().forEach(items -> items.setTimeSeries(new ArrayList<>()));

        final List<Capacity> response = CapacityMapper.mapperResponseToListCapacity(request);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void mapper_shoudReturnEmptyListWhenDataAreEmpty() {
        final StorageCapacityResponse request = getRequest();
        request.getItems().forEach(items -> items.getTimeSeries().forEach(timeSeries -> timeSeries.setData(new ArrayList<>())));

        final List<Capacity> response = CapacityMapper.mapperResponseToListCapacity(request);
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    private static StorageCapacityResponse getRequest() {
        final AggregateStatistics aggregateStatistics1 = new AggregateStatistics();
        aggregateStatistics1.setMax(10F);
        aggregateStatistics1.setMin(1F);
        aggregateStatistics1.setMean(5F);
        final Data data1 = new Data();
        data1.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS)));
        data1.setAggregateStatistics(aggregateStatistics1);

        final AggregateStatistics aggregateStatistics2 = new AggregateStatistics();
        aggregateStatistics2.setMax(10F);
        aggregateStatistics2.setMin(1F);
        aggregateStatistics2.setMean(5F);
        final Data data2 = new Data();
        data2.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS)));
        data2.setAggregateStatistics(aggregateStatistics2);

        final TimeSeries timeSeries = new TimeSeries();
        timeSeries.setData(List.of(data1, data2));

        final Items item = new Items();
        item.setTimeSeries(List.of(timeSeries));

        final StorageCapacityResponse request = new StorageCapacityResponse();
        request.setItems(List.of(item));
        return request;
    }


}
