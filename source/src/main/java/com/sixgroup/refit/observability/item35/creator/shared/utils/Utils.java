package com.sixgroup.refit.observability.item35.creator.shared.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.NUM_DECIMALS;

@Slf4j
public final class Utils {

    private Utils() {
    }

    public static String convertBytesToTeraBytes(BigDecimal bytes) {
        return bytes.divide(new BigDecimal("1024").pow(4), NUM_DECIMALS, RoundingMode.HALF_UP).toString();
    }

}
