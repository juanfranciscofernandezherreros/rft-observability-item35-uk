package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;

import com.sixgroup.refit.observability.item35.creator.configuration.ComponentProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingFileRepositoryTest {

    @InjectMocks
    private ReportingFileRepository reportingFileRepository;
    @Mock
    private ReportingFileKudu reportingFileKudu;
    @Mock
    private ParticipantFileTypeProperties participantFileTypeProperties;
    @Mock
    private RegulatorFileTypeProperties regulatorFileTypeProperties;
    @Mock
    private TrFileTypeProperties trFileTypeProperties;

    @Test
    void findParticipantsByDayAndFileType() {
        doReturn(List.of(new ParticipantDTO())).when(reportingFileKudu)
            .findParticipantsByDayAccountAndFileType(any(), any(), anyList());

        when(participantFileTypeProperties.getREPORT_TYPE_QUERY()).thenReturn(List.of("TAR030", "TAR108", "TSR107",
            "TSR109", "RJ092", "RJCT000"));

        reportingFileRepository.findParticipantsByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findParticipantsByDayAccountAndFileType(any(), any(), anyList());

    }

    @Test
    void findRegulatorByDayAccountAndFileType() {
        doReturn(List.of(new RegulatorDTO())).when(reportingFileKudu)
            .findRegulatorByDayAccountAndFileType(any(), any(), anyList());

        when(regulatorFileTypeProperties.getREPORT_TYPE_QUERY()).thenReturn(List.of("TAR030", "TAR108", "TSR107",
            "TSR109", "RJ092", "RJCT000"));

        reportingFileRepository.findRegulatorByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findRegulatorByDayAccountAndFileType(any(), any(), anyList());
    }

    @Test
    void findTrByDayAccountAndFileType() {
        doReturn(List.of(new TrDTO())).when(reportingFileKudu)
            .findTrByDayAccountAndFileType(any(), any(), any());

        when(trFileTypeProperties.getREPORT_TYPE_QUERY()).thenReturn(List.of("TD107", "RL078"));

        reportingFileRepository.findTrByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findTrByDayAccountAndFileType(any(), any(), any());
    }

}
