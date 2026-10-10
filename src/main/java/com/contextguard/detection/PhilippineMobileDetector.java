package com.contextguard.detection;

import com.contextguard.policy.DetectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PhilippineMobileDetector implements TextDetector {
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(?<!\\d)" +
                    "(?:\\+63[ -]?9\\d{2}|09\\d{2})" +
                    "[ -]?\\d{3}" +
                    "[ -]?\\d{4}" +
                    "(?!\\d)"
    );

    @Override
    public List<Detection> detect(String jsonPath, String text) {
        Objects.requireNonNull(jsonPath, "jsonPath must not be null");
        Objects.requireNonNull(text, "text must not be null");

        if (jsonPath.isBlank()) {
            throw new IllegalArgumentException("jsonPath must not be blank");
        }

        Matcher matcher = MOBILE_PATTERN.matcher(text);
        List<Detection> detections = new ArrayList<>();

        while (matcher.find()) {
            detections.add(new Detection (
                    DetectionType.PHILIPPINE_MOBILE_NUMBER,
                    jsonPath,
                    Optional.of(new TextRange(matcher.start(), matcher.end()))
            ));
        }

        return List.copyOf(detections);
    }
}
