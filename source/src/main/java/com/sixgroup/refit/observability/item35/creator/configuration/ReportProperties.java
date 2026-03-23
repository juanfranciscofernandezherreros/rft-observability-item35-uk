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

    /** Legacy EU regulation: ITEM 32c — TRAAA_REGU_TY_VS_PD_ITEM32c_YYYYMMDD (deprecated, use item35) */
    @Valid
    private ReportItemProperties item32;

    /** EU/UK regulation: ITEM 35b — TRAAA_REGU_TY_VS_PD_ITEM35b_YYYYMMDD */
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
        if (item32 != null && item35 != null) {
            throw new IllegalStateException(
                "Only one of 'component-config.item32' or 'component-config.item35' can be configured at a time.");
        }
        if (item32 != null) return item32;
        if (item35 != null) return item35;
        throw new IllegalStateException(
            "Either 'component-config.item32' (legacy) or 'component-config.item35' (EU/UK) must be configured.");
    }
}
