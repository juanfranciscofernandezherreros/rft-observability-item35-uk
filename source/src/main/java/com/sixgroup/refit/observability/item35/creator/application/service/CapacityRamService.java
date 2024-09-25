package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.CapacityRamRepository;
import com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sixgroup.refit.observability.item35.creator.shared.utils.MathsUtils.percentOfTwoBigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapacityRamService {

    private final CapacityRamRepository capacityRamRepository;

    public List<Capacity> findByCapacityRam(final String dateFrom, final String dateTo) {

        log.debug("Find Capacity Ram by dateFrom={}, dateTo={}", dateFrom, dateTo);

        final List<Capacity> listCapacityRam = capacityRamRepository.findByCapacityRam(dateFrom, dateTo);
        final List<Capacity> listTotalCapacityRam = capacityRamRepository.findTotalCapacityRam(dateFrom, dateTo);

        if (CollectionUtils.isEmpty(listCapacityRam)) {
            return new ArrayList<>();
        }

        final Map<String, List<Capacity>> groupedByTimestamp = listCapacityRam.stream()
            .collect(Collectors.groupingBy(Capacity::getDate));

        final Map<String, List<Capacity>> groupedTotalByTimestamp = listTotalCapacityRam.stream()
            .collect(Collectors.groupingBy(Capacity::getDate));

        final List<Capacity> capacityRams = new ArrayList<>();
        groupedByTimestamp.forEach((timestamp, capacityRamsGroup) -> {
            BigDecimal bytesMax = BigDecimal.ZERO;
            BigDecimal bytesMin = BigDecimal.ZERO;
            BigDecimal bytesMean = BigDecimal.ZERO;
            BigDecimal bytesTotalMax = BigDecimal.ZERO;
            BigDecimal bytesTotalMin = BigDecimal.ZERO;
            BigDecimal bytesTotalMean = BigDecimal.ZERO;

            for (Capacity capacityRam : capacityRamsGroup) {
                bytesMax = bytesMax.add(new BigDecimal(capacityRam.getMax()));
                bytesMin = bytesMin.add(new BigDecimal(capacityRam.getMin()));
                bytesMean = bytesMean.add(new BigDecimal(capacityRam.getMean()));
            }

            final List<Capacity> capacityTotalRamsGroup = groupedTotalByTimestamp.get(timestamp);
            for (Capacity capacityTotalRam : capacityTotalRamsGroup) {
                bytesTotalMax = bytesTotalMax.add(new BigDecimal(capacityTotalRam.getMax()));
                bytesTotalMin = bytesTotalMin.add(new BigDecimal(capacityTotalRam.getMin()));
                bytesTotalMean = bytesTotalMean.add(new BigDecimal(capacityTotalRam.getMean()));
            }

            capacityRams.add(new Capacity(timestamp,
                percentOfTwoBigDecimal(bytesMax, bytesTotalMax),
                percentOfTwoBigDecimal(bytesMin, bytesTotalMin),
                percentOfTwoBigDecimal(bytesMean, bytesTotalMean),
                CapacityConstants.RAM));

        });

        return capacityRams;
    }


}
