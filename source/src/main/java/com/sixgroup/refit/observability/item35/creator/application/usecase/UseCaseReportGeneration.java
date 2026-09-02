package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.application.service.ParticipantService;
import com.sixgroup.refit.observability.item35.creator.application.service.RegulatorService;
import com.sixgroup.refit.observability.item35.creator.application.service.TrService;
import com.sixgroup.refit.observability.item35.creator.configuration.Regulation;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.LazyIterators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;

import static com.sixgroup.refit.observability.item.state.domain.enums.State.*;
import static com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType.REPORT_GENERATION;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.ITEM35_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseReportGeneration implements ItemTypeStrategy {

    private final WriteFileItem35Service<ReportGenerationDto> writeFileReportGenerationService;
    private final ProducerItemService producerItemService;
    private final ParticipantService participantService;
    private final RegulatorService regulatorService;
    private final TrService trService;
    private final FileNameService fileNameService;
    private final StateService stateService;
    private final ItemLog iLog = new ItemLog();
    private final ReportItemProperties reportItemProperties;

    @Override
    public File execute(final ItemCommandDTO itemCommand, final Headers headers) {

        log.info("Starting REPORT_GENERATION use case");
        log.debug("ItemCommand received: {}", itemCommand);

        final String fileName = fileNameService.getFileName(REPORT_GENERATION, itemCommand.getItemDate());

        log.debug("Generated fileName: {}", fileName);
        log.debug("ItemId: {}", ITEM35_ID);
        log.debug("ItemDate received: {}", itemCommand.getItemDate());

        File file = null;

        try {

            log.debug("Updating state: INITIAL STEP");

            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());

            // Calculate dates
            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemCommand.getItemDate());
            final String dateTo = DateUtils.firstDayOfCurrentMonth(itemCommand.getItemDate());

            log.debug("Date range calculated:");
            log.debug("dateFrom: {}", dateFrom);
            log.debug("dateTo: {}", dateTo);

            log.info("Calculating records for date range: [{} to {} and {} ]", dateFrom, dateTo , itemCommand.getItemDate());
            final Iterator<ReportGenerationDto> records = LazyIterators.mergeSorted(
                Comparator.comparing(ReportGenerationDto::getDate),
                participantService.iterateParticipants(dateFrom, dateTo, itemCommand.getItemDate()),
                regulatorService.iterateRegulator(dateFrom, dateTo, itemCommand.getItemDate()),
                trService.iterateTr(dateFrom, dateTo, itemCommand.getItemDate()));

            if (!records.hasNext()) {
                log.error("No data found in report generation. Skipping report generation.");
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("No record status found, skipping file generation").build());

                return null;
            }

            log.debug("Saving information step started");
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVING_INFORMATION);

            log.debug("Writing report file...");
            file = writeFileReportGenerationService.writeFileStreaming(records, itemCommand, fileName);

            log.debug("File successfully written.");
            log.debug("File path: {}", file.getPath());
            log.debug("File absolute path: {}", file.getAbsolutePath());

            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).fileUrl(file.getPath()).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVED_INFORMATION);

            // Send response
            log.debug("Sending response event...");

            itemCommand.setFileName(file.getName());
            itemCommand.setFileUrl(file.getAbsolutePath());

            producerItemService.send(itemCommand, headers);
            log.debug("Response event sent to Kafka");

            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SENT_RESPONSE);
            log.debug("REPORT_GENERATION process finished successfully");

        } catch (Exception e) {
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
            stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("Error to generate file report generation: " + e.getMessage()).build());
        }

        return file;
    }

    @Override
    public ItemType getItemType() {
        return REPORT_GENERATION;
    }
}
