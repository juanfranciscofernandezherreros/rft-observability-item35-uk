package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityRamRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.CapacityRamService;
import com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapacityRamServiceImpl implements CapacityRamService {

    private final CapacityRamRepository capacityRamRepository;

    @Override
    public List<Capacity> findByCapacityRam(String itemDate) {

        RftLog.info("Find Capacity Ram by date");

        List<Capacity> listCapacityRam = capacityRamRepository.
            findByCapacityRam(Utils.getFirstDayOfMonthAndYear(itemDate), Utils.getFirstDayOfNextMonthAndYear(itemDate));

        List<Capacity> capacityRams = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(listCapacityRam)) {
            Map<String, List<Capacity>> groupedByTimestamp = listCapacityRam.stream()
                .collect(Collectors.groupingBy(Capacity::getDate));
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
                    Utils.convertBytesToTeraBytes(bytesMax), Utils.convertBytesToTeraBytes(bytesMin), Utils.convertBytesToTeraBytes(bytesMean), CapacityConstants.RAM));

            });
        }

        return capacityRams;
    }


}
