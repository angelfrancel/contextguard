package com.contextguard.detection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TextRangeTest {
    @Test
    void createsValidRange() {
        TextRange range = new TextRange(3, 8);

        assertEquals(3, range.startInclusive());
        assertEquals(8, range.endExclusive());
    }

    @Test
    void rejectsNegativeStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TextRange(-1, 5)
        );
    }

    @Test
    void rejectsEmptyRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TextRange(5, 5)
        );
    }

    @Test
    void rejectsReversedRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TextRange(8, 5)
        );
    }
}
