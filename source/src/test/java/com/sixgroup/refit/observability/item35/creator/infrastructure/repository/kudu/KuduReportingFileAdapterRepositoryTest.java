package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.TrProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control.KuduReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control.ReportingFileKudu;
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
    private ParticipantProperties participantProperties;
    @Mock
    private RegulatorProperties regulatorProperties;
    @Mock
    private TrProperties trProperties;

    @Test
    void findParticipantsByDayAndFileType() {
        final ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName("TAR030");
        reportConfig1.setReportName("TAR030");
        final ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName("TAR108");
        reportConfig2.setReportName("TAR108");

        doReturn(List.of(new ParticipantDTO())).when(reportingFileKudu)
            .findParticipantsByDayAccountAndFileType(any(), any(), anyList(), any());

        when(participantProperties.getReports()).thenReturn(List.of(reportConfig1, reportConfig2));

        kuduReportingFileAdapterRepository.findParticipantsByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findParticipantsByDayAccountAndFileType(any(), any(), anyList(), any());

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
            .findRegulatorByDayAccountAndFileType(any(), any(), anyList(), any());

        when(regulatorProperties.getReports()).thenReturn(List.of(reportConfig1, reportConfig2));

        kuduReportingFileAdapterRepository.findRegulatorByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findRegulatorByDayAccountAndFileType(any(), any(), anyList(), any());
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
            .findTrByDayAccountAndFileType(any(), any(), any(), any());

        when(trProperties.getReports()).thenReturn(List.of(reportConfig1, reportConfig2));

        kuduReportingFileAdapterRepository.findTrByDayAccountAndFileType("2024-02-01", "2024-03-01");

        verify(reportingFileKudu, times(1)).findTrByDayAccountAndFileType(any(), any(), any(), any());
    }

}
