package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.ParticipantService;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.ParticipantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.utils.Utils.getItemDateFormatted;

@RequiredArgsConstructor
@Service
@Slf4j
public class ParticipantServiceImpl implements ParticipantService {

    private final ReportingFileRepository reportingFileRepository;
    private final ParticipantMapper participantMapper;
    private final ParticipantFileTypeProperties fileTypeProperties;
    @Override
    public List<ReportGenerationDto> findParticipants(final String initDate, final String endDate, String itemDate) {
        List<ParticipantDTO> participants = reportingFileRepository.findParticipantsByDayAccountAndFileType(initDate, endDate);
        List<ReportGenerationDto> participantReportGenerationData = new ArrayList<>();

        if (participants.isEmpty()) {
            return participantReportGenerationData;
        }

        participants.forEach(participant -> {
            ReportGenerationDto reportGenerationDto = participantMapper.toReportGenerationDto(participant, fileTypeProperties);
            reportGenerationDto.setReportingDate(getItemDateFormatted(itemDate));
            participantReportGenerationData.add(reportGenerationDto);
        });

        return participantReportGenerationData;
    }

}
