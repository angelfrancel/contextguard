package com.contextguard.redaction;

import com.contextguard.detection.TextRange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class TextRedactor {

    private static final String PLACEHOLDER = "[REDACTED]";

    public String redact(String text, List<TextRange> ranges) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(ranges, "ranges must not be null");

        List<TextRange> ascendingRanges = ranges.stream()
                .map(range -> Objects.requireNonNull(
                        range,
                        "range must not be null"
                ))
                .sorted(Comparator.comparingInt(TextRange::startInclusive))
                .toList();

        List<TextRange> mergedRanges = new ArrayList<>();

        for (TextRange range : ascendingRanges) {
            if (range.endExclusive() > text.length()) {
                throw new IllegalArgumentException(
                        "range exceeds text length"
                );
            }

            if (mergedRanges.isEmpty()) {
                mergedRanges.add(range);
                continue;
            }

            int lastIndex = mergedRanges.size() - 1;
            TextRange previous = mergedRanges.get(lastIndex);

            if (range.startInclusive() < previous.endExclusive()) {
                mergedRanges.set(
                        lastIndex,
                        new TextRange(
                                previous.startInclusive(),
                                Math.max(
                                        previous.endExclusive(),
                                        range.endExclusive()
                                )
                        )
                );
            } else {
                mergedRanges.add(range);
            }
        }

        StringBuilder result = new StringBuilder(text);

        for (int i = mergedRanges.size() - 1; i >= 0; i--) {
            TextRange range = mergedRanges.get(i);
            result.replace(
                    range.startInclusive(),
                    range.endExclusive(),
                    PLACEHOLDER
            );
        }

        return result.toString();
    }
}