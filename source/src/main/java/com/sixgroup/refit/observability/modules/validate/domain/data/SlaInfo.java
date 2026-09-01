package com.sixgroup.refit.observability.modules.validate.domain.data;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class SlaInfo {
    private Boolean meetsSla;
    private LocalDateTime expectSlaDate;
    private Duration differenceDuration;
    private Duration generationDuration;

    public BigDecimal getDifferenceInBigDecimal() {
        if (differenceDuration == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(differenceDuration.toSeconds())
            .divide(BigDecimal.valueOf(3600), 1, RoundingMode.HALF_UP);
    }

    public LocalDateTime getDifferenceFromInit() {
        LocalDateTime origin = LocalDateTime.of(1900, 1, 1, 0, 0);
        return generationDuration == null ? origin : origin.plus(generationDuration);
    }
}
