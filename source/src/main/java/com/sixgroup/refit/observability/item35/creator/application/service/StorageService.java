package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Data;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.TimeSeries;
import com.sixgroup.refit.observability.item35.creator.domain.repository.StorageCapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.NUM_DECIMALS;


@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final StorageCapacityRepository storageCapacityRepository;

    private final ClouderaProperties clouderaProperties;

    public List<Storage> getTotalCapacity(String dateFrom, String dateTo) {
        StorageCapacityResponse totalStorage = storageCapacityRepository.findTotalStorage(dateFrom, dateTo);
        Optional.ofNullable(totalStorage).orElseThrow(() -> new RuntimeException("'apiCloudera findTotalStorage()' response" +
            " cannot be null"));
        return manageClouderaApiTotalCapacityResponseData(totalStorage);
    }

    public List<Storage> getTotalFreeCapacity(String dateFrom, String dateTo) {
        StorageCapacityResponse freeStorage = storageCapacityRepository.findFreeStorage(dateFrom, dateTo);
        Optional.ofNullable(freeStorage).orElseThrow(() -> new RuntimeException("'apiCloudera findFreeStorage()' response" +
            " cannot be null"));
        return manageClouderaApiTotalCapacityFreeResponseData(freeStorage);
    }

    private List<Storage> manageClouderaApiTotalCapacityResponseData(StorageCapacityResponse storageCapacityResponse) {

        List<Storage> storageList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(storageCapacityResponse.getItems())) {
            storageCapacityResponse.getItems().forEach(items -> {
                if (CollectionUtils.isNotEmpty(items.getTimeSeries())) {
                    List<TimeSeries> filteredTimeseries = items.getTimeSeries().stream()
                        .filter(timeSeries -> null != timeSeries.getMetadata()
                            && StringUtils.isNotBlank(timeSeries.getMetadata().getEntityName())
                            && timeSeries.getMetadata().getEntityName().equals(clouderaProperties.getStorage().getEntityName()))
                        .toList();

                    if (CollectionUtils.isNotEmpty(filteredTimeseries)) {
                        filteredTimeseries.forEach(timeSeries -> {
                            List<Data> dataList = timeSeries.getData();
                            if (CollectionUtils.isNotEmpty(dataList)) {
                                dataList.forEach(dataItem -> {
                                    String timestamp = dataItem.getTimestamp();
                                    Float mean = dataItem.getAggregateStatistics().getMean();
                                    Float totalCapacityInTeras = BigDecimal.valueOf(mean)
                                        .divide(new BigDecimal("1024").pow(4), NUM_DECIMALS, RoundingMode.HALF_UP)
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
            log.debug("Empty list from Cloudera Api TotalCapacity");
            throw new ResourceNotFoundException("Empty list from Cloudera Api 'TotalCapacity' filter");
        }
        return storageList;
    }

    private List<Storage> manageClouderaApiTotalCapacityFreeResponseData(StorageCapacityResponse storageCapacityResponse) {

        List<Storage> storageList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(storageCapacityResponse.getItems())) {
            storageCapacityResponse.getItems().forEach(items -> {
                if (CollectionUtils.isNotEmpty(items.getTimeSeries())) {
                    List<TimeSeries> filteredTimeseries = items.getTimeSeries().stream()
                        .filter(timeSeries -> null != timeSeries.getMetadata()
                            && StringUtils.isNotBlank(timeSeries.getMetadata().getEntityName())
                            && timeSeries.getMetadata().getEntityName().equals(clouderaProperties.getStorage().getEntityName())).toList();
                    if (CollectionUtils.isNotEmpty(filteredTimeseries)) {
                        filteredTimeseries.forEach(timeSeries -> {
                            List<Data> dataList = timeSeries.getData();
                            if (CollectionUtils.isNotEmpty(dataList)) {
                                dataList.forEach(dataItem -> {
                                    String timestamp = dataItem.getTimestamp();
                                    Float max = dataItem.getAggregateStatistics().getMax();
                                    Float maxFreeDataInTeras = BigDecimal.valueOf(max)
                                        .divide(new BigDecimal("1024").pow(4), NUM_DECIMALS, RoundingMode.HALF_UP)
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
            log.debug("Empty list from Cloudera Api 'TotalFreeCapacity' filter");
            throw new ResourceNotFoundException("Empty list from Cloudera Api 'TotalFreeCapacity' filter");
        }
        return storageList;
    }

}
