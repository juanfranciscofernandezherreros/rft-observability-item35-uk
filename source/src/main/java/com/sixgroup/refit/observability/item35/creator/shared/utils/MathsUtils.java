package com.sixgroup.refit.observability.item35.creator.shared.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public final class MathsUtils {

    public static final String PERCENT = "1";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String pattern = "#0.0000";

    private MathsUtils() {
    }

    public static Boolean isIntoMayorPercent(final BigDecimal initValue, final BigDecimal newValue) {
        final BigDecimal bigDecimalFivePercent = MathsUtils.percentOf(new BigDecimal(PERCENT), initValue);
        final BigDecimal minor = initValue.add(bigDecimalFivePercent.negate());
        final BigDecimal mayor = initValue.add(bigDecimalFivePercent);
        return newValue.compareTo(minor) < 0 || newValue.compareTo(mayor) > 0;
    }

    public static BigDecimal percentOf(final BigDecimal percentage, final BigDecimal total) {
        return percentage.multiply(total).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public static String percentOfTwoBigDecimal(final BigDecimal value, final BigDecimal total) {
        final BigDecimal result = value
            .multiply(ONE_HUNDRED)
            .divide(total, 2, RoundingMode.HALF_UP)
            .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
        return formatBigDecimalToFourDecimals(result);
    }

    public static String formatBigDecimalToFourDecimals(final BigDecimal value) {
        final DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(value).replace(",", ".");
    }
}
