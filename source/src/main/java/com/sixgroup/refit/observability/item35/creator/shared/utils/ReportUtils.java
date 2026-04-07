package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public final class ReportUtils {

    private ReportUtils() {
    }

    public static List<String> getReportsTypeQuery(final List<ReportConfig> reports) {
        return reports.stream().map(ReportConfig::getName).distinct().toList();
    }

    public static String getReportName(final List<ReportConfig> reports, final String reportName) {
        final Optional<String> reportFound =
            reports.stream().filter(report -> report.getName().equals(reportName)).map(ReportConfig::getReportName).findFirst();
        if (reportFound.isEmpty()) {
            log.error("Error to find report with reportName {}", reportName);
            return null;
        }
        return reportFound.get();
    }
}
