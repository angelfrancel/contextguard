package com.contextguard.detection;

import com.contextguard.policy.DetectionType;

import java.util.Objects;
import java.util.Optional;

public record Detection(
        DetectionType type,
        String jsonPath,
        Optional<TextRange> textRange
) {

    public Detection {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(jsonPath, "jsonPath must not be null");
        Objects.requireNonNull(textRange, "textRange must not be null");

        if (jsonPath.isBlank()) {
            throw new IllegalArgumentException("jsonPath must not be blank");
        }
    }
}