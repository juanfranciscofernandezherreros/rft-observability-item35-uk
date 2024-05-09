package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KuduReportingFileAdapterRepositoryTest {

    @InjectMocks
    private KuduReportingFileAdapterRepository kuduReportingFileAdapterRepository;
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
        final ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName("TAR030");
        reportConfig1.setReportName("TAR030");
        final ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName("TAR108");
        reportConfig2.setReportName("TAR108");

        doReturn(List.of(new ParticipantDTO())).when(reportingFileKudu)
            .findParticipantsByDayAccountAndFileType(any(), any(), anyList());

        when(participantFileTypeProperties.getReports()).thenReturn(List.of(reportConfig1, reportConfig2));

        kuduReportingFileAdapterRepository.findParticipantsByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findParticipantsByDayAccountAndFileType(any(), any(), anyList());

    }

    @Test
    void findRegulatorByDayAccountAndFileType() {
        final ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName("TAR030");
        reportConfig1.setReportName("TAR030");
        final ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName("TAR108");
        reportConfig2.setReportName("TAR108");

        doReturn(List.of(new RegulatorDTO())).when(reportingFileKudu)
            .findRegulatorByDayAccountAndFileType(any(), any(), anyList());

        when(regulatorFileTypeProperties.getReports()).thenReturn(List.of(reportConfig1, reportConfig2));

        kuduReportingFileAdapterRepository.findRegulatorByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findRegulatorByDayAccountAndFileType(any(), any(), anyList());
    }

    @Test
    void findTrByDayAccountAndFileType() {
        final ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName("TD107");
        reportConfig1.setReportName("TD107");
        final ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName("RL078");
        reportConfig2.setReportName("RL078");

        doReturn(List.of(new TrDTO())).when(reportingFileKudu)
            .findTrByDayAccountAndFileType(any(), any(), any());

        when(trFileTypeProperties.getReports()).thenReturn(List.of(reportConfig1, reportConfig2));

        kuduReportingFileAdapterRepository.findTrByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findTrByDayAccountAndFileType(any(), any(), any());
    }

}
