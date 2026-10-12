package com.contextguard.redaction;

import com.contextguard.detection.Detection;
import com.contextguard.detection.TextRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ArgumentRedactor {

    private static final String PLACEHOLDER = "[REDACTED]";

    private final TextRedactor textRedactor = new TextRedactor();

    public Map<String, Object> redact(
            Map<String, Object> arguments,
            List<Detection> detectionsToRedact
    ) {
        Objects.requireNonNull(arguments, "arguments must not be null");
        Objects.requireNonNull(
                detectionsToRedact,
                "detectionsToRedact must not be null"
        );

        Map<String, List<Detection>> detectionsByPath = new HashMap<>();

        for (Detection detection : detectionsToRedact) {
            Objects.requireNonNull(detection, "detection must not be null");

            detectionsByPath
                    .computeIfAbsent(
                            detection.jsonPath(),
                            ignored -> new ArrayList<>()
                    )
                    .add(detection);
        }

        Set<String> handledPaths = new HashSet<>();
        Set<String> wholeValuePaths = new HashSet<>();

        Map<String, Object> result = copyMap(
                arguments,
                "$",
                detectionsByPath,
                handledPaths,
                wholeValuePaths
        );

        for (Detection detection : detectionsToRedact) {
            String path = detection.jsonPath();

            if (!handledPaths.contains(path)
                    && !isCoveredByWholeValue(path, wholeValuePaths)) {
                throw new IllegalArgumentException(
                        "detection path not found: " + path
                );
            }
        }

        return result;
    }

    private Map<String, Object> copyMap(
            Map<?, ?> source,
            String path,
            Map<String, List<Detection>> detectionsByPath,
            Set<String> handledPaths,
            Set<String> wholeValuePaths
    ) {
        Map<String, Object> copy = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String fieldName)) {
                throw new IllegalArgumentException(
                        "JSON object field names must be strings"
                );
            }

            String fieldPath = path + "." + fieldName;

            copy.put(
                    fieldName,
                    copyValue(
                            entry.getValue(),
                            fieldPath,
                            detectionsByPath,
                            handledPaths,
                            wholeValuePaths
                    )
            );
        }

        return copy;
    }

    private Object copyValue(
            Object value,
            String path,
            Map<String, List<Detection>> detectionsByPath,
            Set<String> handledPaths,
            Set<String> wholeValuePaths
    ) {
        List<Detection> detections = detectionsByPath.getOrDefault(
                path,
                List.of()
        );

        if (!detections.isEmpty()) {
            handledPaths.add(path);

            boolean redactWholeValue = detections.stream()
                    .anyMatch(detection -> detection.textRange().isEmpty());

            if (redactWholeValue) {
                wholeValuePaths.add(path);
                return PLACEHOLDER;
            }

            if (!(value instanceof String text)) {
                throw new IllegalArgumentException(
                        "text range does not point to a string: " + path
                );
            }

            List<TextRange> ranges = detections.stream()
                    .map(detection -> detection.textRange().orElseThrow())
                    .toList();

            return textRedactor.redact(text, ranges);
        }

        if (value instanceof Map<?, ?> map) {
            return copyMap(
                    map,
                    path,
                    detectionsByPath,
                    handledPaths,
                    wholeValuePaths
            );
        }

        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());

            for (int index = 0; index < list.size(); index++) {
                copy.add(
                        copyValue(
                                list.get(index),
                                path + "[" + index + "]",
                                detectionsByPath,
                                handledPaths,
                                wholeValuePaths
                        )
                );
            }

            return copy;
        }

        if (value == null
                || value instanceof String
                || value instanceof Number
                || value instanceof Boolean) {
            return value;
        }

        throw new IllegalArgumentException(
                "unsupported argument value at: " + path
        );
    }

    private boolean isCoveredByWholeValue(
            String path,
            Set<String> wholeValuePaths
    ) {
        for (String wholeValuePath : wholeValuePaths) {
            if (path.startsWith(wholeValuePath + ".")
                    || path.startsWith(wholeValuePath + "[")) {
                return true;
            }
        }

        return false;
    }
}