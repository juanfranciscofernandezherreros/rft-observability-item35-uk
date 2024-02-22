package com.sixgroup.refit.observability.item35.creator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("component-config.csv")
public class CsvProperties {
    private String outputPath;
}
