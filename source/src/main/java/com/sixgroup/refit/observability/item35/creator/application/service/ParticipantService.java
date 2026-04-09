package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.ParticipantMapper;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.PARTICIPANT_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final ParticipantProperties fileTypeProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final ParticipantMapper participantMapper = new ParticipantMapper();

    public List<ReportGenerationDto> findParticipants(final String initDate, final String endDate, final String itemDate) {
        log.debug("[START] Entering findParticipants method.");
        log.debug("[INPUT PARAMETERS] initDate: '{}', endDate: '{}', itemDate: '{}'", initDate, endDate, itemDate);
        // 1. Fetching first group of participants
        log.debug("[QUERY] Executing findParticipantsByDayAccountAndFileType for range {} to {}", initDate, endDate);
        final List<ParticipantDTO> participants = reportingFileAdapterRepository.findParticipantsByDayAccountAndFileType(initDate, endDate);
        log.debug("[QUERY RESULT] findParticipantsByDayAccountAndFileType returned {} records.", participants.size());
        // 2. Fetching second group of participants (Reco)
        log.debug("[QUERY] Executing findParticipantsRecoFileType for range {} to {}", initDate, endDate);
        final List<ParticipantDTO> participantsReco = reportingFileAdapterRepository.findParticipantsRecoFileType(initDate, endDate);
        log.debug("[QUERY RESULT] findParticipantsRecoFileType returned {} records.", participantsReco.size());
        // 3. Merging lists
        final List<ParticipantDTO> totalParticipants = new ArrayList<>();
        totalParticipants.addAll(participants);
        totalParticipants.addAll(participantsReco);
        log.debug("[DATA MERGE] Total participants combined: {}. Proceeding to process list.", totalParticipants.size());

        if (totalParticipants.isEmpty()) {
            log.error("[TERMINATION] No participants found to process. Returning empty list.");
            return new ArrayList<>();
        }

        final List<ReportGenerationDto> participantReportGenerationData = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        int totalSize = totalParticipants.size();

        // 4. Processing each participant
        totalParticipants.forEach(participant -> {
            int currentIndex = counter.getAndIncrement();
            log.debug("[ITERATION {}/{}] Processing Participant: FileType='{}', Session='{}', InitDate='{}', EndDate='{}'",
                currentIndex, totalSize, participant.getFileType(), participant.getReportingSession(),
                participant.getInitDate(), participant.getEndDate());

            log.debug("[SLA LOOKUP] Searching SlaInfo for Entity: '{}', FileType: '{}', Session: '{}'",
                PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession());

            final Optional<SlaInfo> slaInfo = slaInfoRepository.getSlaInfo(
                PARTICIPANT_ENTITY,
                participant.getFileType(),
                participant.getReportingSession(),
                participant.getInitDate(),
                participant.getEndDate()
            );

            if (slaInfo.isEmpty()) {
                log.debug("[SLA NOT FOUND] SlaInfo is missing for entity: {}, report: {}, session: {}",
                    PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession());

                // Keeping the specific error log format as per your original requirement
                log.error("10.4 Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession(), participant.getEndDate());
            } else {
                log.debug("[SLA FOUND] Successfully retrieved SlaInfo: {}", slaInfo.get());

                log.debug("[MAPPING] Mapping ParticipantDTO and SlaInfo to ReportGenerationDto...");
                final ReportGenerationDto reportGenerationDto = participantMapper.toReportGenerationDto(participant, fileTypeProperties, slaInfo.get());

                log.debug("[DATE FORMATTING] Formatting itemDate '{}' using DateUtils...", itemDate);
                String formattedDate = DateUtils.itemDateFormatted(itemDate);
                reportGenerationDto.setReportingDate(formattedDate);

                log.debug("[SUCCESS] ReportGenerationDto created for {}. Final ReportingDate: {}",
                    participant.getFileType(), reportGenerationDto.getReportingDate());

                participantReportGenerationData.add(reportGenerationDto);
            }
        });

        log.debug("[FINISH] findParticipants completed. Total ReportGenerationDto objects generated: {}/{}",
            participantReportGenerationData.size(), totalSize);

        return participantReportGenerationData;
    }
}
