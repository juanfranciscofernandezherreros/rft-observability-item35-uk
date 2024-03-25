package com.sixgroup.refit.observability.item35.creator.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Data;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.TimeSeries;
import com.sixgroup.refit.observability.item35.creator.domain.repository.StorageCapacityRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.StorageService;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.NODE_STORAGE_TOTAL;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageCapacityRepository storageCapacityRepository;

    @Override
    public List<Storage> getTotalCapacity(String dateFrom, String dateTo) {
        Response totalStorage = storageCapacityRepository.findTotalStorage(dateFrom, dateTo);
        Optional.ofNullable(totalStorage).orElseThrow(() -> new RuntimeException("'apiCloudera findTotalStorage()' response" +
            " cannot be null"));
        return manageClouderaApiTotalCapacityResponseData(totalStorage);
    }

    @Override
    public List<Storage> getTotalFreeCapacity(String dateFrom, String dateTo) {
        Response freeStorage = storageCapacityRepository.findFreeStorage(dateFrom, dateTo);
        Optional.ofNullable(freeStorage).orElseThrow(() -> new RuntimeException("'apiCloudera findFreeStorage()' response" +
            " cannot be null"));
        return manageClouderaApiTotalCapacityFreeResponseData(freeStorage);
    }

    private static List<Storage> manageClouderaApiTotalCapacityResponseData(Response response) {

        List<Storage> storageList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(response.getItems())) {
            response.getItems().forEach(items -> {
                if (CollectionUtils.isNotEmpty(items.getTimeSeries())) {
                    List<TimeSeries> filteredTimeseries = items.getTimeSeries().stream()
                        .filter(timeSeries -> null != timeSeries.getMetadata()
                            && StringUtils.isNotBlank(timeSeries.getMetadata().getEntityName())
                            && timeSeries.getMetadata().getEntityName().equals(NODE_STORAGE_TOTAL))
                        .toList();

                    if (CollectionUtils.isNotEmpty(filteredTimeseries)) {
                        filteredTimeseries.forEach(timeSeries -> {
                            List<Data> dataList = timeSeries.getData();
                            if (CollectionUtils.isNotEmpty(dataList)) {
                                dataList.forEach(dataItem -> {
                                    String timestamp = dataItem.getTimestamp();
                                    Float mean = dataItem.getAggregateStatistics().getMean();
                                    Float totalCapacityInTeras = new BigDecimal(mean)
                                        .divide(new BigDecimal("1024").pow(4), 3, RoundingMode.HALF_UP)
                                        .floatValue();
                                    Storage storage = new Storage(timestamp, totalCapacityInTeras);
                                    storageList.add(storage);
                                });
                            }
                        });
                    }
                }
            });
        }
        if (CollectionUtils.isEmpty(storageList)) {
            RftLog.info("Empty list from Cloudera Api TotalCapacity");
            throw new ResourceNotFoundException("Empty list from Cloudera Api 'TotalCapacity' filter");
        }
        return storageList;
    }

    private static List<Storage> manageClouderaApiTotalCapacityFreeResponseData(Response response) {

        List<Storage> storageList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(response.getItems())) {
            response.getItems().forEach(items -> {
                if (CollectionUtils.isNotEmpty(items.getTimeSeries())) {
                    List<TimeSeries> filteredTimeseries = items.getTimeSeries().stream()
                        .filter(timeSeries -> null != timeSeries.getMetadata()
                            && StringUtils.isNotBlank(timeSeries.getMetadata().getEntityName())
                            && timeSeries.getMetadata().getEntityName().equals(NODE_STORAGE_TOTAL)).toList();
                    if (CollectionUtils.isNotEmpty(filteredTimeseries)) {
                        filteredTimeseries.forEach(timeSeries -> {
                            List<Data> dataList = timeSeries.getData();
                            if (CollectionUtils.isNotEmpty(dataList)) {
                                dataList.forEach(dataItem -> {
                                    String timestamp = dataItem.getTimestamp();
                                    Float max = dataItem.getAggregateStatistics().getMax();
                                    Float maxFreeDataInTeras = new BigDecimal(max)
                                        .divide(new BigDecimal("1024").pow(4), 3, RoundingMode.HALF_UP)
                                        .floatValue();
                                    Storage storage = new Storage(timestamp, maxFreeDataInTeras);
                                    storageList.add(storage);
                                });
                            }
                        });
                    }
                }
            });
        }

        if (CollectionUtils.isEmpty(storageList)) {
            RftLog.info("Empty list from Cloudera Api 'TotalFreeCapacity' filter");
            throw new ResourceNotFoundException("Empty list from Cloudera Api 'TotalFreeCapacity' filter");
        }
        return storageList;
    }

}
