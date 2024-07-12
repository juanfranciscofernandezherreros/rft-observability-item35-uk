package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.CapacityRamRepository;
import com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapacityRamService {

    private final CapacityRamRepository capacityRamRepository;

    public List<Capacity> findByCapacityRam(final String dateFrom, final String dateTo) {

        log.debug("Find Capacity Ram by dateFrom={}, dateTo={}", dateFrom, dateTo);

        final List<Capacity> listCapacityRam = capacityRamRepository.findByCapacityRam(dateFrom, dateTo);

        if (CollectionUtils.isEmpty(listCapacityRam)) {
            return new ArrayList<>();
        }

        final Map<String, List<Capacity>> groupedByTimestamp = listCapacityRam.stream()
            .collect(Collectors.groupingBy(Capacity::getDate));

        final List<Capacity> capacityRams = new ArrayList<>();
        groupedByTimestamp.forEach((timestamp, capacityRamsGroup) -> {
            BigDecimal bytesMax = BigDecimal.ZERO;
            BigDecimal bytesMin = BigDecimal.ZERO;
            BigDecimal bytesMean = BigDecimal.ZERO;

            for (Capacity capacityRam : capacityRamsGroup) {
                bytesMax = bytesMax.add(new BigDecimal(capacityRam.getMax()));
                bytesMin = bytesMin.add(new BigDecimal(capacityRam.getMin()));
                bytesMean = bytesMean.add(new BigDecimal(capacityRam.getMean()));
            }
            capacityRams.add(new Capacity(timestamp,
                Utils.convertBytesToTeraBytes(bytesMax), Utils.convertBytesToTeraBytes(bytesMin),
                Utils.convertBytesToTeraBytes(bytesMean), CapacityConstants.RAM));

        });

        return capacityRams;
    }


}
