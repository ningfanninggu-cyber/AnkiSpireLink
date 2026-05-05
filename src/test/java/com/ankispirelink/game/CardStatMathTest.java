package com.ankispirelink.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CardStatMathTest {

    @Test
    void multipliesOnlyPositiveScalarValues() {
        assertEquals(12, CardStatMath.multiplyPositive(6, 2));
        assertEquals(0, CardStatMath.multiplyPositive(0, 2));
        assertEquals(-1, CardStatMath.multiplyPositive(-1, 2));
        assertEquals(6, CardStatMath.multiplyPositive(6, 1));
    }

    @Test
    void multipliesOnlyPositiveAoeDamageValuesAndDoesNotMutateInput() {
        int[] original = new int[]{5, 0, -1, 7};

        int[] multiplied = CardStatMath.multiplyPositiveValues(original, 3);

        assertArrayEquals(new int[]{15, 0, -1, 21}, multiplied);
        assertArrayEquals(new int[]{5, 0, -1, 7}, original);
    }

    @Test
    void preservesNullAoeDamageArray() {
        assertNull(CardStatMath.multiplyPositiveValues(null, 4));
    }
}
