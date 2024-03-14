package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityCpuRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.CapacityCpuService;
import com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CapacityCpuServiceImpl implements CapacityCpuService {

    private final CapacityCpuRepository capacityCpuRepository;

    @Override
    public List<Capacity> findByCapacityCpu(String itemDate) {

        RftLog.info("Find Capacity Cpu by date");

        List<Capacity> capacityMonthList = capacityCpuRepository.
            findByCapacityCpu(Utils.getFirstDayOfMonthAndYear(itemDate), Utils.getFirstDayOfNextMonthAndYear(itemDate));

        List<Capacity> capacityCpu = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(capacityMonthList)) {
            capacityMonthList.forEach(capacityDay -> {
                BigDecimal minValue = BigDecimal.valueOf(Double.parseDouble(capacityDay.getMin())).divide(BigDecimal.valueOf(100), 3, RoundingMode.UNNECESSARY);
                BigDecimal maxValue = BigDecimal.valueOf(Double.parseDouble(capacityDay.getMax())).divide(BigDecimal.valueOf(100), 3, RoundingMode.UNNECESSARY);
                BigDecimal mean = BigDecimal.valueOf(Double.parseDouble(capacityDay.getMean())).divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);

                capacityDay.setMin(minValue.toString());
                capacityDay.setMax(maxValue.toString());
                capacityDay.setMean(mean.toString());

                capacityCpu.add(new Capacity(capacityDay.getDate(), maxValue.toString(), minValue.toString(), mean.toString(), CapacityConstants.CPU));

            });
        }
        return capacityCpu;
    }
}
