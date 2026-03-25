package com.sixgroup.refit.observability.item35.creator.configuration;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "component-config")
public class ReportProperties {

    @Valid
    private ReportItemProperties item35;

    /**
     * Exposes the active {@link ReportItemProperties} as a bean.
     * Exactly one of {@code item32} (legacy) or {@code item35} (EU/UK) must be configured.
     *
     * @throws IllegalStateException if neither or both are configured
     */
    @Bean
    public ReportItemProperties reportItemProperties() {
        return item35;
    }
}
