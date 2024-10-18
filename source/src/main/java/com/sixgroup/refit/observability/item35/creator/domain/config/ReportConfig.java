package com.sixgroup.refit.observability.item35.creator.domain.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportConfig {
    private String name;
    private String reportName;
    private String reportQueryEod;
}
