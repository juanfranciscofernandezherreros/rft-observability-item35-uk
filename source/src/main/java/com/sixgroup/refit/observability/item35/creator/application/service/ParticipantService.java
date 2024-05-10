package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
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

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.PARTICIPANT_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final ParticipantFileTypeProperties fileTypeProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final ParticipantMapper participantMapper = new ParticipantMapper();

    public List<ReportGenerationDto> findParticipants(final String initDate, final String endDate, final String itemDate) {
        final List<ParticipantDTO> participants =
            reportingFileAdapterRepository.findParticipantsByDayAccountAndFileType(initDate, endDate);
        if (participants.isEmpty()) {
            return new ArrayList<>();
        }

        final List<ReportGenerationDto> participantReportGenerationData = new ArrayList<>();
        participants.forEach(participant -> {
            final Optional<SlaInfo> slaInfo =
                slaInfoRepository.getSlaInfo(PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession(), participant.getInitDate(), participant.getEndDate());
            if (slaInfo.isEmpty()) {
                log.error("Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
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
