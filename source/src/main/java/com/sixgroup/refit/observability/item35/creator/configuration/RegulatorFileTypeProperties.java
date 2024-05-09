package com.sixgroup.refit.observability.item35.creator.configuration;

import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties("component-config.regulator")
public class RegulatorFileTypeProperties {

    private String reportTypeEsma;
    private String reportTypeNca;
    private List<ReportConfig> reports = new ArrayList<>();
}
