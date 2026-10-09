package com.contextguard.detection;

import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailDetectorTest {

    private final EmailDetector detector = new EmailDetector();

    @Test
    void detectsEmailAndReportsItsLocation() {
        String text = "Contact angel@example.com today";

        List<Detection> detections = detector.detect(
                "$.message",
                text
        );

        assertEquals(1, detections.size());

        Detection detection = detections.getFirst();
        TextRange range = detection.textRange().orElseThrow();

        assertEquals(DetectionType.EMAIL, detection.type());
        assertEquals("$.message", detection.jsonPath());
        assertEquals("angel@example.com", text.substring(
                range.startInclusive(),
                range.endExclusive()
        ));
    }

    @Test
    void detectsMultipleEmails() {
        String text = "Send to first@example.com and second@test.org";

        List<Detection> detections = detector.detect(
                "$.message",
                text
        );

        assertEquals(2, detections.size());
    }

    @Test
    void returnsEmptyListWhenEmailIsAbsent() {
        List<Detection> detections = detector.detect(
                "$.message",
                "This text contains no email address"
        );

        assertTrue(detections.isEmpty());
    }

    @Test
    void returnsImmutableDetectionList() {
        List<Detection> detections = detector.detect(
                "$.message",
                "Contact angel@example.com"
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> detections.add(detections.getFirst())
        );
    }

    @Test
    void rejectsNullText() {
        assertThrows(
                NullPointerException.class,
                () -> detector.detect("$.message", null)
        );
    }
}