package com.contextguard.redaction;

import com.contextguard.detection.TextRange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextRedactorTest {

    private final TextRedactor redactor = new TextRedactor();

    @Test
    void redactsOverlappingRangesOnce() {
        String result = redactor.redact(
                "abcdefghij",
                List.of(
                        new TextRange(2, 8),
                        new TextRange(4, 6)
                )
        );

        assertEquals("ab[REDACTED]ij", result);
    }

    @Test
    void redactsSeparateRanges() {
        String result = redactor.redact(
                "AA secret ZZ token",
                List.of(
                        new TextRange(3, 9),
                        new TextRange(13, 18)
                )
        );

        assertEquals("AA [REDACTED] ZZ [REDACTED]", result);
    }

    @Test
    void leavesTextUnchangedWhenThereAreNoRanges() {
        assertEquals("hello", redactor.redact("hello", List.of()));
    }

    @Test
    void rejectsRangeBeyondTextLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> redactor.redact(
                        "hello",
                        List.of(new TextRange(1, 6))
                )
        );
    }
}