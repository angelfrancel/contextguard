package com.contextguard.detection;

import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhilippineMobileDetectorTest {

    private final PhilippineMobileDetector detector = new PhilippineMobileDetector();

    @Test
    void detectsCompactLocalNumber() {
        String text = "Call 09171234567 today";

        List<Detection> detections = detector.detect("$.message", text);

        assertEquals(1, detections.size());

        Detection detection = detections.getFirst();
        TextRange range = detection.textRange().orElseThrow();

        assertEquals(DetectionType.PHILIPPINE_MOBILE_NUMBER, detection.type());
        assertEquals("$.message", detection.jsonPath());
        assertEquals("09171234567", text.substring(range.startInclusive(), range.endExclusive()));
    }

    @Test
    void detectsFormattedInternationalNumber() {
        String text = "Call +63 917 123 4567 today";

        List<Detection> detections = detector.detect("$.message", text);

        assertEquals(1, detections.size());

        TextRange range = detections.getFirst().textRange().orElseThrow();

        assertEquals("+63 917 123 4567", text.substring(range.startInclusive(), range.endExclusive()));
    }

    @Test
    void detectsMultipleNumbers() {
        String text = "Primary: 0917-123-4567, secondary: +639181234567";

        List<Detection> detections = detector.detect("$.message", text);

        assertEquals(2, detections.size());
    }

    @Test
    void ignoresNonMobilePrefix() {
        List<Detection> detections = detector.detect("$.message", "This is not mobile: 08171234567");

        assertTrue(detections.isEmpty());
    }

    @Test
    void ignoresNumberWithTooManyDigits() {
        List<Detection> detections = detector.detect("$.message", "Invalid: 091712345678");

        assertTrue(detections.isEmpty());
    }

    @Test
    void rejectsNullText() {
        assertThrows(NullPointerException.class, () -> detector.detect("$.message", null));
    }

}
