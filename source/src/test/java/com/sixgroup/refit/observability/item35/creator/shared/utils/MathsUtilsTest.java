package com.sixgroup.refit.observability.item35.creator.shared.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MathsUtilsTest {

    @Test
    void isIntoMayorPercent_shouldReturnTrueWhenPercentIsMayor() {
        final BigDecimal initValue = new BigDecimal("861.371");

        final BigDecimal percent = MathsUtils.percentOf(new BigDecimal("1").add(new BigDecimal("1")), initValue);

        final BigDecimal newValue = initValue.add(percent);

        final Boolean response = MathsUtils.isIntoMayorPercent(initValue, newValue);
        assertTrue(response);
    }

    @Test
    void isIntoMayorPercent_shouldReturnFalseWhenPercentIsMinor() {
        final BigDecimal initValue = new BigDecimal("861.371");

        final BigDecimal percent = MathsUtils.percentOf(new BigDecimal("0.5"), initValue);

        final BigDecimal newValue = initValue.add(percent);

        final Boolean response = MathsUtils.isIntoMayorPercent(initValue, newValue);
        assertFalse(response);
    }

    @ParameterizedTest
    @CsvSource({
        "861.371, 861.3475", // Q1
        "861.371, 861.363", // Q2
        "861.371, 861.3629", // Q3
        "861.371, 861.3691" // Q4
    })
    void testProdData(final String initValueRequest, final String newValueRequest) {
        final BigDecimal initValue = new BigDecimal(initValueRequest);
        final BigDecimal newValue = new BigDecimal(newValueRequest);

        final Boolean response = MathsUtils.isIntoMayorPercent(initValue, newValue);
        assertFalse(response);
    }

    @ParameterizedTest
    @CsvSource({
        "0.0030, 0.0030", // Q1
        "0.0080, 0.008" // Q2
    })
    void formatBigDecimal(final String expected, final String current) {
        final BigDecimal currentValue = new BigDecimal(current);

        assertEquals(expected, MathsUtils.formatBigDecimalToFourDecimals(currentValue));
    }
}
