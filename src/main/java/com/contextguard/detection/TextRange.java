package com.contextguard.detection;

public record TextRange(int startInclusive, int endExclusive) {

    public TextRange {
        if (startInclusive < 0) {
            throw new IllegalArgumentException(
                    "startInclusive must be non-negative"
            );
        }

        if (endExclusive <= startInclusive) {
            throw new IllegalArgumentException(
                    "endExclusive must be greater than startInclusive"
            );
        }
    }
}