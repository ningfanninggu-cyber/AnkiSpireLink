package com.ankispirelink.game;

public final class CardStatMath {
    private CardStatMath() {
    }

    public static int multiplyPositive(int value, int multiplier) {
        return value > 0 && multiplier > 1 ? value * multiplier : value;
    }

    public static int[] multiplyPositiveValues(int[] values, int multiplier) {
        if (values == null) {
            return null;
        }
        int[] multiplied = values.clone();
        if (multiplier <= 1) {
            return multiplied;
        }
        for (int i = 0; i < multiplied.length; i++) {
            if (multiplied[i] > 0) {
                multiplied[i] *= multiplier;
            }
        }
        return multiplied;
    }
}
