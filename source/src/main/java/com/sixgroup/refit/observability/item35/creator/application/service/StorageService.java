package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Data;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.TimeSeries;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.StorageCapacityRepository;
import com.sixgroup.refit.observability.item35.creator.shared.exception.InternalErrorException;
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

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.NUM_DECIMALS;


@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final StorageCapacityRepository storageCapacityRepository;

    private final ClouderaProperties clouderaProperties;

    public List<Storage> getTotalCapacity(final String dateFrom, final String dateTo) {
        final Optional<StorageCapacityResponse> totalStorage = storageCapacityRepository.findTotalStorage(dateFrom, dateTo);
        if (totalStorage.isEmpty()) {
            throw new InternalErrorException("'apiCloudera getTotalCapacity()' response cannot be null with dateFrom " + dateFrom + " and dateTo " + dateTo);
        }
        return manageClouderaApiTotalCapacityResponseData(totalStorage.get());
    }

    public List<Storage> getTotalFreeCapacity(final String dateFrom, final String dateTo) {
        final Optional<StorageCapacityResponse> freeStorage = storageCapacityRepository.findFreeStorage(dateFrom, dateTo);
        if (freeStorage.isEmpty()) {
            throw new InternalErrorException("'apiCloudera getTotalFreeCapacity()' response cannot be null dateFrom " + dateFrom + " and dateTo " + dateTo);
        }
        return manageClouderaApiTotalCapacityFreeResponseData(freeStorage.get());
    }

    private List<Storage> manageClouderaApiTotalCapacityResponseData(final StorageCapacityResponse storageCapacityResponse) {
        if (CollectionUtils.isEmpty(storageCapacityResponse.getItems())) {
            return new ArrayList<>();
        }

        final List<Storage> storageList = new ArrayList<>();
        storageCapacityResponse.getItems().forEach(items -> {
            if (CollectionUtils.isNotEmpty(items.getTimeSeries())) {
                final List<TimeSeries> filteredTimeSeries = items.getTimeSeries().stream()
                    .filter(timeSeries -> null != timeSeries.getMetadata()
                        && StringUtils.isNotBlank(timeSeries.getMetadata().getEntityName())
                        && timeSeries.getMetadata().getEntityName().equals(clouderaProperties.getStorage().getEntityName()))
                    .toList();

                if (CollectionUtils.isNotEmpty(filteredTimeSeries)) {
                    filteredTimeSeries.forEach(timeSeries -> {
                        final List<Data> dataList = timeSeries.getData();
                        if (CollectionUtils.isNotEmpty(dataList)) {
                            dataList.forEach(dataItem -> storageList.add(new Storage(dataItem.getTimestamp(), getCapacitiesInTeras(dataItem.getAggregateStatistics().getMean()))));
                        }
                    });
                }
            }
        });
        if (CollectionUtils.isEmpty(storageList)) {
            log.debug("Empty list from Cloudera Api TotalCapacity");
            throw new ResourceNotFoundException("Empty list from Cloudera Api 'TotalCapacity' filter");
        }
        return storageList;
    }

    private List<Storage> manageClouderaApiTotalCapacityFreeResponseData(final StorageCapacityResponse storageCapacityResponse) {
        if (CollectionUtils.isEmpty(storageCapacityResponse.getItems())) {
            return new ArrayList<>();
        }

        final List<Storage> storageList = new ArrayList<>();
        storageCapacityResponse.getItems().forEach(items -> {
            if (CollectionUtils.isNotEmpty(items.getTimeSeries())) {
                final List<TimeSeries> filteredTimeSeries = items.getTimeSeries().stream()
                    .filter(timeSeries -> null != timeSeries.getMetadata()
                        && StringUtils.isNotBlank(timeSeries.getMetadata().getEntityName())
                        && timeSeries.getMetadata().getEntityName().equals(clouderaProperties.getStorage().getEntityName())).toList();

                if (CollectionUtils.isNotEmpty(filteredTimeSeries)) {
                    filteredTimeSeries.forEach(timeSeries -> {
                        final List<Data> dataList = timeSeries.getData();
                        if (CollectionUtils.isNotEmpty(dataList)) {
                            dataList.forEach(dataItem -> storageList.add(new Storage(dataItem.getTimestamp(), getCapacitiesInTeras(dataItem.getAggregateStatistics().getMax()))));
                        }
                    });
                }
            }
        });
        if (CollectionUtils.isEmpty(storageList)) {
            log.debug("Empty list from Cloudera Api 'TotalFreeCapacity' filter");
            throw new ResourceNotFoundException("Empty list from Cloudera Api 'TotalFreeCapacity' filter");
        }
        return storageList;
    }

    private BigDecimal getCapacitiesInTeras(final Float value) {
        return BigDecimal.valueOf(value).divide(new BigDecimal("1024").pow(4), NUM_DECIMALS, RoundingMode.HALF_UP);
    }

}
