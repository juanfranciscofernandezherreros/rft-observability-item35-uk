package com.sixgroup.refit.observability.item35.creator.configuration;

import com.sixgroup.refit.observability.item35.creator.domain.config.TranslationData;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("component-config")
public class ReportProperties {

    @NotNull
    private String trCode;
    @NotNull
    private String regulationReference;

    private Translation translation;

    @Getter
    @Setter
    public static class Translation {
        private final List<TranslationData> accounts = new ArrayList<>();
        private final List<TranslationData> reports = new ArrayList<>();

    }
}
