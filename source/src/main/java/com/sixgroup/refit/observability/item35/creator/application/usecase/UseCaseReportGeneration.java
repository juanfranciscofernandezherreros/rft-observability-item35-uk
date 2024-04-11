package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.*;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.CREATING_AND_SAVING_FILE;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;

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

    @Override
    public File execute(ItemCommandDTO itemCommandDTO, Headers headers) {
        log.debug("Generating Report Generation file ...");
        File fileReportGeneration;
        try {
            stateService.nextStep(
                StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            LogService.logInfo(CREATING_AND_SAVING_FILE, itemCommandDTO);

            String firstDayOfMonth = Utils.getFirstDayOfMonthAndYear(itemCommandDTO.getItemDate());
            String firstDayOfNextMonth = Utils.getFirstDayOfNextMonthAndYear(itemCommandDTO.getItemDate());

            List<ReportGenerationDto> participants = participantService.findParticipants(firstDayOfMonth,
                firstDayOfNextMonth, itemCommandDTO.getItemDate());

            List<ReportGenerationDto> regulators = regulatorService.findRegulator(firstDayOfMonth,
                firstDayOfNextMonth, itemCommandDTO.getItemDate());

            List<ReportGenerationDto> trs = trService.findTr(firstDayOfMonth,
                firstDayOfNextMonth, itemCommandDTO.getItemDate());

            List<ReportGenerationDto> joinedCollection = Stream.concat(Stream.concat(participants.stream(),
                regulators.stream()), trs.stream()).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(joinedCollection)) {
                log.error("No data found in report generation, skipping report generation");
                throw new
                    ResourceNotFoundException("No data found in report generation, skipping report generation");
            }

            List<ReportGenerationDto> orderedCollection = Utils.getOrderCollectionsByDate(joinedCollection);

            fileReportGeneration = writeFileReportGenerationService.writeFile(orderedCollection, itemCommandDTO);
            log.debug("Generated report generation file");
            itemCommandDTO.setFileUrl(fileReportGeneration.toString());
            itemCommandDTO.setFileName(fileReportGeneration.getName());
            producerItemService.send(itemCommandDTO, headers);
            return fileReportGeneration;
        } catch (Exception e) {
            log.error("Error to generate file report generation: {}", e.getMessage(), e);
            stateService.setError(
                StateRequest.builder()
                    .fileName(Utils.getFileName(itemCommandDTO))
                    .itemType(ITEM35)
                    .errorDescription("Error to generate file report generation: " + e.getMessage())
                    .build());
            if (e instanceof ResourceNotFoundException) {
                throw new RuntimeException(e.getMessage());
            }
            throw new RuntimeException(e);

        }
    }

    @Override
    public ItemType getItemType() {
        return ItemType.REPORT_GENERATION;
    }


}
