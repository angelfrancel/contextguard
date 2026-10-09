package com.contextguard.detection;

import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetectionTest {

    @Test
    void createsDetectionWithTextRange() {
        TextRange range = new TextRange(6, 23);

        Detection detection = new Detection(
                DetectionType.EMAIL,
                "$.message",
                Optional.of(range)
        );

        assertEquals(DetectionType.EMAIL, detection.type());
        assertEquals("$.message", detection.jsonPath());
        assertEquals(Optional.of(range), detection.textRange());
    }

    @Test
    void createsWholeValueDetectionWithoutTextRange() {
        Detection detection = new Detection(
                DetectionType.SENSITIVE_JSON_FIELD,
                "$.password",
                Optional.empty()
        );

        assertEquals(DetectionType.SENSITIVE_JSON_FIELD, detection.type());
        assertEquals("$.password", detection.jsonPath());
        assertTrue(detection.textRange().isEmpty());
    }

    @Test
    void rejectsBlankJsonPath() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Detection(
                        DetectionType.EMAIL,
                        " ",
                        Optional.of(new TextRange(0, 5))
                )
        );
    }

    @Test
    void rejectsNullDetectionType() {
        assertThrows(
                NullPointerException.class,
                () -> new Detection(
                        null,
                        "$.message",
                        Optional.of(new TextRange(0, 5))
                )
        );
    }

    @Test
    void rejectsNullOptionalRange() {
        assertThrows(
                NullPointerException.class,
                () -> new Detection(
                        DetectionType.EMAIL,
                        "$.message",
                        null
                )
        );
    }
}