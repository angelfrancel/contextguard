package com.contextguard.detection;

import com.contextguard.policy.DetectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmailDetector implements TextDetector {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?<![A-Za-z0-9._%+-])" +
                    "[A-Za-z0-9._%+-]+@" +
                    "[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+" +
                    "(?![A-Za-z0-9._%+-])"
    );

    @Override
    public List<Detection> detect(String jsonPath, String text) {
        Objects.requireNonNull(jsonPath, "jsonPath must not be null");
        Objects.requireNonNull(text, "text must not be null");

        if (jsonPath.isBlank()) {
            throw new IllegalArgumentException("jsonPath must not be blank");
        }

        Matcher matcher = EMAIL_PATTERN.matcher(text);
        List<Detection> detections = new ArrayList<>();

        while (matcher.find()) {
            TextRange range = new TextRange(
                    matcher.start(),
                    matcher.end()
            );

            detections.add(
                    new Detection(
                            DetectionType.EMAIL,
                            jsonPath,
                            Optional.of(range)
                    )
            );
        }

        return List.copyOf(detections);
    }
}