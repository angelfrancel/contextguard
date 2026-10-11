package com.contextguard.detection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DetectionPipeline {

    private final List<TextDetector> textDetectors;
    private final SensitiveJsonFieldDetector
            sensitiveJsonFieldDetector;

    public DetectionPipeline(
            List<TextDetector> textDetectors,
            SensitiveJsonFieldDetector
                    sensitiveJsonFieldDetector
    ) {
        Objects.requireNonNull(
                textDetectors,
                "textDetectors must not be null"
        );

        this.textDetectors =
                List.copyOf(textDetectors);

        this.sensitiveJsonFieldDetector =
                Objects.requireNonNull(
                        sensitiveJsonFieldDetector,
                        "sensitiveJsonFieldDetector must not be null"
                );
    }

    public List<Detection> detect(
            Map<String, Object> arguments
    ) {
        Objects.requireNonNull(
                arguments,
                "arguments must not be null"
        );

        List<Detection> detections =
                new ArrayList<>();

        detections.addAll(
                sensitiveJsonFieldDetector.detect(arguments)
        );

        inspectValue(
                arguments,
                "$",
                detections
        );

        return List.copyOf(detections);
    }

    private void inspectValue(
            Object value,
            String currentPath,
            List<Detection> detections
    ) {
        if (value instanceof String text) {
            inspectText(
                    currentPath,
                    text,
                    detections
            );
            return;
        }

        if (value instanceof Map<?, ?> map) {
            inspectMap(
                    map,
                    currentPath,
                    detections
            );
            return;
        }

        if (value instanceof List<?> list) {
            inspectList(
                    list,
                    currentPath,
                    detections
            );
        }
    }

    private void inspectText(
            String jsonPath,
            String text,
            List<Detection> detections
    ) {
        for (TextDetector detector : textDetectors) {
            detections.addAll(
                    detector.detect(jsonPath, text)
            );
        }
    }

    private void inspectMap(
            Map<?, ?> map,
            String currentPath,
            List<Detection> detections
    ) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String fieldName)) {
                throw new IllegalArgumentException(
                        "JSON object field names must be strings"
                );
            }

            inspectValue(
                    entry.getValue(),
                    currentPath + "." + fieldName,
                    detections
            );
        }
    }

    private void inspectList(
            List<?> list,
            String currentPath,
            List<Detection> detections
    ) {
        for (int index = 0; index < list.size(); index++) {
            inspectValue(
                    list.get(index),
                    currentPath + "[" + index + "]",
                    detections
            );
        }
    }
}