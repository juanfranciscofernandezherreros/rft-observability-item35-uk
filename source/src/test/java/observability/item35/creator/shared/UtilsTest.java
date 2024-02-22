package observability.item35.creator.shared;

import com.sixgroup.refit.observability.item35.creator.shared.Utils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @ParameterizedTest
    @MethodSource("provideDatesForFirstDayOfPreviousMonth")
    void testGetFirstDayOfPreviousMonth(LocalDate inputDate, String expectedFirstDay) {
        assertEquals(expectedFirstDay, Utils.getFirstDayOfPreviousMonth(inputDate));
    }

    private static Stream<Arguments> provideDatesForFirstDayOfPreviousMonth() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 2, 20), "2024-01-01"),
                Arguments.of(LocalDate.of(2024, 1, 1), "2023-12-01"),
                Arguments.of(LocalDate.of(2024, 3, 15),"2024-02-01"));
    }

    @ParameterizedTest
    @MethodSource("provideDatesForLastDayOfPreviousMonth")
    void testGetLastDayOfPreviousMonth(LocalDate inputDate, String expectedLastDay) {
        assertEquals(expectedLastDay, Utils.getLastDayOfPreviousMonth(inputDate));
    }

    private static Stream<Arguments> provideDatesForLastDayOfPreviousMonth() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 2, 20), "2024-01-31"),
                Arguments.of(LocalDate.of(2024, 1, 1), "2023-12-31"),
                Arguments.of(LocalDate.of(2024, 3, 15),"2024-02-29")
        );
    }
}
