package com.contextguard.detection;

import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveJsonFieldDetectorTest {

    private final SensitiveJsonFieldDetector detector =
            new SensitiveJsonFieldDetector();

    @Test
    void detectsTopLevelSensitiveField() {
        Map<String, Object> arguments =
                Map.of(
                        "username", "angel",
                        "password", "fake-secret-value"
                );

        List<Detection> detections =
                detector.detect(arguments);

        assertEquals(1, detections.size());

        Detection detection = detections.getFirst();

        assertEquals(
                DetectionType.SENSITIVE_JSON_FIELD,
                detection.type()
        );
        assertEquals(
                "$.password",
                detection.jsonPath()
        );
        assertTrue(
                detection.textRange().isEmpty()
        );
    }

    @Test
    void detectsNestedSensitiveField() {
        Map<String, Object> arguments =
                Map.of(
                        "profile",
                        Map.of(
                                "displayName", "Angel",
                                "apiKey", "fake-api-key"
                        )
                );

        List<Detection> detections =
                detector.detect(arguments);

        assertEquals(1, detections.size());
        assertEquals(
                "$.profile.apiKey",
                detections.getFirst().jsonPath()
        );
    }

    @Test
    void detectsSensitiveFieldInsideArray() {
        Map<String, Object> arguments =
                Map.of(
                        "users",
                        List.of(
                                Map.of(
                                        "name", "First",
                                        "token", "fake-token"
                                )
                        )
                );

        List<Detection> detections =
                detector.detect(arguments);

        assertEquals(1, detections.size());
        assertEquals(
                "$.users[0].token",
                detections.getFirst().jsonPath()
        );
    }

    @Test
    void matchesFieldNamesCaseInsensitively() {
        Map<String, Object> arguments =
                Map.of(
                        "PASSWORD",
                        "fake-secret-value"
                );

        List<Detection> detections =
                detector.detect(arguments);

        assertEquals(1, detections.size());
    }

    @Test
    void ignoresOrdinaryFieldNames() {
        Map<String, Object> arguments =
                Map.of(
                        "name", "Angel",
                        "message", "Hello",
                        "count", 3
                );

        List<Detection> detections =
                detector.detect(arguments);

        assertTrue(detections.isEmpty());
    }

    @Test
    void doesNotStoreRawSensitiveValue() {
        String sensitiveValue =
                "must-never-appear-in-detection";

        Map<String, Object> arguments =
                Map.of(
                        "password",
                        sensitiveValue
                );

        Detection detection =
                detector.detect(arguments).getFirst();

        assertFalse(
                detection.toString()
                        .contains(sensitiveValue)
        );
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(
                NullPointerException.class,
                () -> detector.detect(null)
        );
    }
}