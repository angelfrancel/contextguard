package com.contextguard.detection;

import com.contextguard.policy.DetectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SensitiveJsonFieldDetector {

    private static final Set<String> SENSITIVE_FIELD_NAMES =
            Set.of(
                    "password",
                    "passwd",
                    "secret",
                    "apikey",
                    "api_key",
                    "token",
                    "accesstoken",
                    "access_token",
                    "refreshtoken",
                    "refresh_token",
                    "authorization",
                    "privatekey",
                    "private_key"
            );

    public List<Detection> detect(
            Map<String, Object> arguments
    ) {
        Objects.requireNonNull(
                arguments,
                "arguments must not be null"
        );

        List<Detection> detections =
                new ArrayList<>();

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
        if (value instanceof Map<?, ?> map) {
            inspectMap(map, currentPath, detections);
            return;
        }

        if (value instanceof List<?> list) {
            inspectList(list, currentPath, detections);
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

            String fieldPath =
                    currentPath + "." + fieldName;

            if (isSensitiveFieldName(fieldName)) {
                detections.add(
                        new Detection(
                                DetectionType.SENSITIVE_JSON_FIELD,
                                fieldPath,
                                Optional.empty()
                        )
                );

                continue;
            }

            inspectValue(
                    entry.getValue(),
                    fieldPath,
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

    private boolean isSensitiveFieldName(
            String fieldName
    ) {
        String normalizedName =
                fieldName.toLowerCase(Locale.ROOT);

        return SENSITIVE_FIELD_NAMES.contains(
                normalizedName
        );
    }
}