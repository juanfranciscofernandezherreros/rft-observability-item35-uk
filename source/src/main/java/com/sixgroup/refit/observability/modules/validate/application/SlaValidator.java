package com.sixgroup.refit.observability.modules.validate.application;

import com.sixgroup.refit.observability.app.properties.SlaProperties;
import com.sixgroup.refit.observability.modules.validate.domain.data.Report;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class SlaValidator {
    private final SlaProperties properties;

    public SlaValidator(SlaProperties properties) {
        this.properties = properties;
    }

    public Optional<SlaInfo> getSlaInfo(String entity, String reportName, LocalDateTime reportSession,
                                        LocalDateTime reportDate) {
        return getSlaInfo(entity, reportName, reportSession, reportSession, reportDate);
    }

    public Optional<SlaInfo> getSlaInfo(String entity, String reportName, LocalDateTime reportSession,
                                        LocalDateTime reportInitDate, LocalDateTime reportEndDate) {
        return findReport(entity, reportName).map(report -> {
            LocalDateTime expected = reportSession.toLocalDate().plusDays(1)
                .atTime(extractTime(report.getSlaEnd()));
            Duration generation = Duration.between(reportInitDate, reportEndDate);
            Duration difference = reportEndDate.isAfter(expected) ? Duration.between(expected, reportEndDate) : null;
            return SlaInfo.builder().meetsSla(!reportEndDate.isAfter(expected)).expectSlaDate(expected)
                .differenceDuration(difference).generationDuration(generation).build();
        });
    }

    private Optional<Report> findReport(String entity, String reportName) {
        List<Report> reports = properties.getEntity().entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(entity))
            .map(java.util.Map.Entry::getValue).findFirst().orElse(List.of());
        return reports.stream().filter(report -> reportName.equalsIgnoreCase(report.getName())).findFirst();
    }

    private LocalTime extractTime(String pattern) {
        if (pattern == null) {
            return LocalTime.MIDNIGHT;
        }
        int separator = pattern.indexOf('T');
        String time = separator >= 0 ? pattern.substring(separator + 1) : pattern;
        return LocalTime.parse(time.replace("Z", "").toUpperCase(Locale.ROOT));
    }
}
