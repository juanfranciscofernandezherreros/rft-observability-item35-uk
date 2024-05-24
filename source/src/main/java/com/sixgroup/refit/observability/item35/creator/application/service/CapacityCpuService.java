package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityCpuRepository;
import com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.NUM_DECIMALS;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapacityCpuService {

    private final CapacityCpuRepository capacityCpuRepository;

    public List<Capacity> findByCapacityCpu(final String itemDate) {

        log.debug("Find Capacity Cpu by date {}", itemDate);

        final List<Capacity> capacityMonthList = capacityCpuRepository.
            findByCapacityCpu(DateUtils.firstDayOfMonth(itemDate), DateUtils.firstDayOfNextMonth(itemDate));

        if (CollectionUtils.isEmpty(capacityMonthList)) {
            return new ArrayList<>();
        }

        final List<Capacity> capacityCpu = new ArrayList<>();
        capacityMonthList.forEach(capacityDay -> {
            final BigDecimal minValue = BigDecimal.valueOf(Double.parseDouble(capacityDay.getMin())).divide(BigDecimal.valueOf(100), NUM_DECIMALS, RoundingMode.UNNECESSARY);
            final BigDecimal maxValue = BigDecimal.valueOf(Double.parseDouble(capacityDay.getMax())).divide(BigDecimal.valueOf(100), NUM_DECIMALS, RoundingMode.UNNECESSARY);
            final BigDecimal mean = BigDecimal.valueOf(Double.parseDouble(capacityDay.getMean())).divide(BigDecimal.valueOf(100), NUM_DECIMALS, RoundingMode.HALF_UP);

            capacityDay.setMin(minValue.toString());
            capacityDay.setMax(maxValue.toString());
            capacityDay.setMean(mean.toString());

            capacityCpu.add(new Capacity(capacityDay.getDate(), maxValue.toString(), minValue.toString(), mean.toString(), CapacityConstants.CPU));

        });
        return capacityCpu;
    }
}
