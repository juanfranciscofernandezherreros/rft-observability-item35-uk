package com.sixgroup.refit.observability.item35.creator.configuration;

import com.sixgroup.refit.observability.item35.creator.domain.config.TranslationData;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.HEADER_SLA_BREACH_ID;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.ITEM32_ID;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.ITEM35_ID;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants.HEADER_TR_INCIDENT_ID;

@Getter
@Setter
public class ReportItemProperties {

    public String getIncidentIdHeader() {
        return regulation == Regulation.EU ? HEADER_SLA_BREACH_ID : HEADER_TR_INCIDENT_ID;
    }

    public String getEffectiveItemId() {
        return regulation == Regulation.EU ? ITEM32_ID : ITEM35_ID;
    }

    @NotNull
    private Regulation regulation = Regulation.EU;

    @NotNull
    private String trCode;

    @NotNull
    private String regulationReference;

    @NotNull
    private String submissionVolumesFileNamePattern;

    @NotNull
    private String reportGenerationFileNamePattern;

    @NotNull
    private String storageCapacityFileNamePattern;

    @NotNull
    private String computeCapacityFileNamePattern;

    private Translation translation;

    @Getter
    @Setter
    public static class Translation {
        private final List<TranslationData> accounts = new ArrayList<>();
        private final List<TranslationData> reports = new ArrayList<>();
    }
}
