package com.sixgroup.refit.observability.item35.creator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("component-config.regulator")
public class RegulatorFileTypeProperties {

    private String REPORT_TYPE_ESMA;
    private String REPORT_TYPE_NCA;
    private List<String> REPORT_TYPE_QUERY;
    private Map<String, String> TYPES;

}
