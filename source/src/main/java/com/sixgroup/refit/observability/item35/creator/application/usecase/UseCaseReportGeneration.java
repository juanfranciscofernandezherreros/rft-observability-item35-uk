package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
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
import com.sixgroup.refit.observability.item35.creator.shared.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.sixgroup.refit.observability.item.state.domain.enums.State.*;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.ITEM35;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseReportGeneration implements ItemTypeStrategy {

    private final WriteFileItem35Service<ReportGenerationDto> writeFileReportGenerationService;
    private final ProducerItemService producerItemService;
    private final StateService stateService;
    private final ParticipantService participantService;
    private final RegulatorService regulatorService;
    private final TrService trService;
    private final ItemLog iLog = new ItemLog();

    @Override
    public File execute(final ItemCommandDTO itemCommand, final Headers headers) {
        log.debug("Generating Report Generation file ...");
        File file = null;
        try {
            stateService.nextStep(StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).build());

            //Find information
            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemCommand.getItemDate());
            final String dateTo = DateUtils.firstDayOfCurrentMonth(itemCommand.getItemDate());

            final List<ReportGenerationDto> participants = participantService.findParticipants(dateFrom, dateTo, itemCommand.getItemDate());
            final List<ReportGenerationDto> regulators = regulatorService.findRegulator(dateFrom, dateTo, itemCommand.getItemDate());
            final List<ReportGenerationDto> trs = trService.findTr(dateFrom, dateTo, itemCommand.getItemDate());

            final List<ReportGenerationDto> joinedCollection = Stream.concat(Stream.concat(participants.stream(),
                regulators.stream()), trs.stream()).toList();

            if (CollectionUtils.isEmpty(joinedCollection)) {
                iLog.info(ItemReportingDto.builder().itemType(ITEM35).build(), ERROR);
                log.error("No data found in report generation, skipping report generation");
                stateService.setError(StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).errorDescription("No record status found, skipping file generation").build());
                return null;
            }

            //Saving information
            stateService.nextStep(StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).build());
            iLog.info(itemCommand, SAVING_INFORMATION);

            //Saved information
            final List<ReportGenerationDto> orderedCollection = CollectionsUtils.getOrderCollectionsByDate(joinedCollection);
            file = writeFileReportGenerationService.writeFile(orderedCollection, itemCommand);
            stateService.nextStep(StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).fileUrl(file.getPath()).build());
            iLog.info(itemCommand, SAVED_INFORMATION);

            //Sent response
            itemCommand.setFileName(file.getName());
            itemCommand.setFileUrl(file.getAbsolutePath());
            producerItemService.send(itemCommand, headers);
            stateService.nextStep(StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).build());
            iLog.info(itemCommand, SENT_RESPONSE);
        } catch (Exception e) {
            iLog.info(ItemReportingDto.builder().itemType(ITEM35).build(), ERROR);
            log.error("Error to generate file report generation: {}", e.getMessage(), e);
            stateService.setError(StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).errorDescription("Error to generate file report generation: " + e.getMessage()).build());
        }
        return file;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.REPORT_GENERATION;
    }


}
