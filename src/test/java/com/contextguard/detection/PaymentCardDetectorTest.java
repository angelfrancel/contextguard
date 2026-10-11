package com.contextguard.detection;

import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentCardDetectorTest {

    private final PaymentCardDetector detector = new PaymentCardDetector(
                    new LuhnValidator()
            );

    @Test
    void detectsCompactValidCardCandidate() {
        String text = "Use test card 4111111111111111";

        List<Detection> detections = detector.detect("$.payment", text);

        assertEquals(1, detections.size());
        assertEquals(DetectionType.PAYMENT_CARD, detections.getFirst().type());
        assertEquals("$.payment", detections.getFirst().jsonPath());
    }

    @Test
    void reportsOriginalFormattedCandidateRange() {
        String text = "Use test card 4111-1111-1111-1111 today";

        List<Detection> detections = detector.detect("$.payment", text);

        assertEquals(1, detections.size());

        TextRange range = detections.getFirst()
                .textRange()
                .orElseThrow();

        assertEquals("4111-1111-1111-1111", text.substring(range.startInclusive(), range.endExclusive()));
    }

    @Test
    void detectsMultipleValidCandidates() {
        String text = "Cards: 4111111111111111 and 5555555555554444";

        List<Detection> detections = detector.detect("$.message", text);

        assertEquals(2, detections.size());
    }

    @Test
    void ignoresCandidateThatFailsLuhn() {
        List<Detection> detections = detector.detect("$.payment", "Invalid card: 4111111111111112");

        assertTrue(detections.isEmpty());
    }

    @Test
    void ignoresCandidateWithTooFewDigits() {
        List<Detection> detections = detector.detect("$.payment", "Too short: 411111111111");

        assertTrue(detections.isEmpty());
    }

    @Test
    void ignoresCandidateWithTooManyDigits() {
        List<Detection> detections = detector.detect("$.payment", "Too long: 41111111111111111111");

        assertTrue(detections.isEmpty());
    }

    @Test
    void returnsImmutableDetectionList() {
        List<Detection> detections = detector.detect("$.payment", "Card: 4111111111111111");

        assertThrows(
                UnsupportedOperationException.class,
                () -> detections.add(
                        detections.getFirst()
                )
        );
    }

    @Test
    void rejectsNullValidator() {
        assertThrows(
                NullPointerException.class,
                () -> new PaymentCardDetector(null)
        );
    }
}