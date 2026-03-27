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
        final List<ParticipantDTO> participants = reportingFileAdapterRepository.findParticipantsByDayAccountAndFileType(initDate, endDate);
        log.info("10.1 Query findParticipantsByDayAccountAndFileType finalizada. Registros encontrados: {} para el rango {} - {}",
            participants.size(), initDate, endDate);
        final List<ParticipantDTO> participantsReco = reportingFileAdapterRepository.findParticipantsRecoFileType(initDate, endDate);
        log.info("10.2 Query findParticipantsRecoFileType finalizada. Registros encontrados: {} para el rango {} - {}",
            participantsReco.size(), initDate, endDate);
        final List<ParticipantDTO> totalParticipants = new ArrayList<>();
        totalParticipants.addAll(participants);
        totalParticipants.addAll(participantsReco);
        if (totalParticipants.isEmpty()) {
            log.info("10.3 No data fount for range {} - {}", initDate, endDate);
            return new ArrayList<>();
        }

        final List<ReportGenerationDto> participantReportGenerationData = new ArrayList<>();
        totalParticipants.forEach(participant -> {
            final Optional<SlaInfo> slaInfo =
                slaInfoRepository.getSlaInfo(PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession(), participant.getInitDate(), participant.getEndDate());
            if (slaInfo.isEmpty()) {
                log.error("10.4 Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession(), participant.getEndDate());
            } else {
                final ReportGenerationDto reportGenerationDto = participantMapper.toReportGenerationDto(participant, fileTypeProperties, slaInfo.get());
                reportGenerationDto.setReportingDate(DateUtils.itemDateFormatted(itemDate));
                participantReportGenerationData.add(reportGenerationDto);
            }
        });

        return participantReportGenerationData;
    }

}
