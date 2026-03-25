package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.application.service.ParticipantService;
import com.sixgroup.refit.observability.item35.creator.application.service.RegulatorService;
import com.sixgroup.refit.observability.item35.creator.application.service.TrService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.CollectionsUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

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

    @Override
    public File execute(final ItemCommandDTO itemCommand, final Headers headers) {

        log.info("--------------------------------------------------");
        log.info("Starting REPORT_GENERATION use case");
        log.info("ItemCommand received: {}", itemCommand);
        log.info("--------------------------------------------------");

        final String fileName = fileNameService.getFileName(REPORT_GENERATION, itemCommand.getItemDate());

        log.info("Generated fileName: {}", fileName);
        log.info("ItemId: {}", ITEM35_ID);
        log.info("ItemDate received: {}", itemCommand.getItemDate());

        File file = null;

        try {

            log.info("Updating state: INITIAL STEP");
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());

            // Calculate dates
            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemCommand.getItemDate());
            final String dateTo = DateUtils.firstDayOfCurrentMonth(itemCommand.getItemDate());

            log.info("Date range calculated:");
            log.info("dateFrom: {}", dateFrom);
            log.info("dateTo: {}", dateTo);

            // Retrieve data
            log.info("Retrieving participants...");
            final List<ReportGenerationDto> participants = participantService.findParticipants(dateFrom, dateTo, itemCommand.getItemDate());
            log.info("Participants found: {}", participants.size());

            log.info("Retrieving regulators...");
            final List<ReportGenerationDto> regulators = regulatorService.findRegulator(dateFrom, dateTo, itemCommand.getItemDate());
            log.info("Regulators found: {}", regulators.size());

            log.info("Retrieving TRs...");
            final List<ReportGenerationDto> trs = trService.findTr(dateFrom, dateTo, itemCommand.getItemDate());
            log.info("TRs found: {}", trs.size());

            // Join collections
            final List<ReportGenerationDto> joinedCollection = Stream.concat(Stream.concat(participants.stream(), regulators.stream()), trs.stream()).toList();
            log.info("Total records after joining collections: {}", joinedCollection.size());

            if (CollectionUtils.isEmpty(joinedCollection)) {
                log.error("No data found in report generation. Skipping report generation.");
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("No record status found, skipping file generation").build());

                return null;
            }

            // Saving information
            log.info("Saving information step started");
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVING_INFORMATION);

            // Order data
            log.info("Ordering records by date...");
            final List<ReportGenerationDto> orderedCollection = CollectionsUtils.getOrderCollectionsByDate(joinedCollection);
            log.info("Ordered collection size: {}", orderedCollection.size());

            // Write file
            log.info("Writing report file...");
            file = writeFileReportGenerationService.writeFile(orderedCollection, itemCommand, fileName);

            log.info("File successfully written.");
            log.info("File path: {}", file.getPath());
            log.info("File absolute path: {}", file.getAbsolutePath());

            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).fileUrl(file.getPath()).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVED_INFORMATION);

            // Send response
            log.info("Sending response event...");

            itemCommand.setFileName(file.getName());
            itemCommand.setFileUrl(file.getAbsolutePath());

            producerItemService.send(itemCommand, headers);
            log.info("Response event sent to Kafka");

            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SENT_RESPONSE);
            log.info("REPORT_GENERATION process finished successfully");

        } catch (Exception e) {
            log.error("Error while generating report generation file: {}", e.getMessage(), e);
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
