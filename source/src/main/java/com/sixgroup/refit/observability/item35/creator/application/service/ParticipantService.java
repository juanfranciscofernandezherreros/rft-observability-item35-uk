package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.ParticipantMapper;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.LazyIterators;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
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

    public Iterator<ReportGenerationDto> iterateParticipants(final String initDate, final String endDate,
                                                              final String itemDate) {
        Iterator<ReportGenerationDto> participants = mapParticipants(
            reportingFileAdapterRepository.iterateParticipantsByDayAccountAndFileType(initDate, endDate), itemDate);
        Iterator<ReportGenerationDto> participantsReco = mapParticipants(
            reportingFileAdapterRepository.iterateParticipantsRecoFileType(initDate, endDate), itemDate);

        return LazyIterators.mergeSorted(
            Comparator.comparing(ReportGenerationDto::getDate), participants, participantsReco);
    }

    private Iterator<ReportGenerationDto> mapParticipants(Iterator<ParticipantDTO> participants, String itemDate) {
        return LazyIterators.filterMap(participants, participant -> {
            Optional<SlaInfo> slaInfo = slaInfoRepository.getSlaInfo(
                PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession(),
                participant.getInitDate(), participant.getEndDate());

            if (slaInfo.isEmpty()) {
                log.error("10.4 Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    PARTICIPANT_ENTITY, participant.getFileType(), participant.getReportingSession(), participant.getEndDate());
                return Optional.empty();
            }

            ReportGenerationDto result = participantMapper.toReportGenerationDto(
                participant, fileTypeProperties, slaInfo.get());
            result.setReportingDate(DateUtils.itemDateFormatted(itemDate));
            return Optional.of(result);
        });
    }

    public List<ReportGenerationDto> findParticipants(final String initDate, final String endDate,
                                                       final String itemDate) {
        List<ReportGenerationDto> results = new ArrayList<>();
        iterateParticipants(initDate, endDate, itemDate).forEachRemaining(results::add);
        return results;
    }
}
