package com.sixgroup.refit.observability.item35.creator.shared;

import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @ParameterizedTest
    @MethodSource("provideDatesForFirstDayOfPreviousMonth")
    void testGetFirstDayOfPreviousMonth(String inputDate, String expectedFirstDay) {
        assertEquals(expectedFirstDay, Utils.getFirstDayOfMonthAndYear(inputDate));
    }

    private static Stream<Arguments> provideDatesForFirstDayOfPreviousMonth() {
        return Stream.of(
            Arguments.of("20240220", "2024-02-01"),
            Arguments.of("20240101", "2024-01-01"),
            Arguments.of("20240315", "2024-03-01"));
    }

    @ParameterizedTest
    @MethodSource("provideDatesForLastDayOfPreviousMonth")
    void testGetLastDayOfPreviousMonth(String inputDate, String expectedLastDay) {
        assertEquals(expectedLastDay, Utils.getLastDayOfMonthAndYear(inputDate));
    }

    private static Stream<Arguments> provideDatesForLastDayOfPreviousMonth() {
        return Stream.of(
            Arguments.of("20240220", "2024-02-29"),
            Arguments.of("20240101", "2024-01-31"),
            Arguments.of("20230215", "2023-02-28")
        );
    }

}
