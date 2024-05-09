package com.sixgroup.refit.observability.item35.creator.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("component-config")
public class ReportProperties {

    @NotNull
    private String trCode;
    @NotNull
    private String regulationReference;
}
