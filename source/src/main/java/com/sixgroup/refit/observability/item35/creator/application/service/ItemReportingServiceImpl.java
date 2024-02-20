package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReporting;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ItemReportingRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.ItemReportingService;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemReportingServiceImpl implements ItemReportingService {

    private final ItemReportingRepository itemReportingRepository;

    @Override
    public void insertItemReporting(ItemReporting itemReporting) {
        RftLog.info("Insert item Report");
           itemReportingRepository.insertItemReporting(itemReporting);
    }
}
