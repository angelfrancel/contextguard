package com.contextguard.detection;

import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationTokenDetectorTest {

    private final AuthenticationTokenDetector detector =
            new AuthenticationTokenDetector();

    @Test
    void detectsBearerTokenWithoutIncludingScheme() {
        String text =
                "Authorization: Bearer abc123_XYZ-789";

        List<Detection> detections =
                detector.detect("$.headers.authorization", text);

        assertEquals(1, detections.size());

        Detection detection = detections.getFirst();
        TextRange range = detection.textRange().orElseThrow();

        assertEquals(
                DetectionType.AUTHENTICATION_TOKEN,
                detection.type()
        );
        assertEquals(
                "abc123_XYZ-789",
                text.substring(
                        range.startInclusive(),
                        range.endExclusive()
                )
        );
    }

    @Test
    void treatsBearerSchemeCaseInsensitively() {
        String text = "bEaReR test-token-123";

        List<Detection> detections =
                detector.detect("$.authorization", text);

        assertEquals(1, detections.size());
    }

    @Test
    void detectsStandaloneJwtLikeToken() {
        String token =
                "eyJhbGciOiJIUzI1NiJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIn0." +
                        "signature123";

        List<Detection> detections =
                detector.detect("$.token", token);

        assertEquals(1, detections.size());

        TextRange range = detections.getFirst()
                .textRange()
                .orElseThrow();

        assertEquals(
                token,
                token.substring(
                        range.startInclusive(),
                        range.endExclusive()
                )
        );
    }

    @Test
    void reportsBearerJwtOnlyOnce() {
        String text =
                "Bearer " +
                        "eyJhbGciOiJIUzI1NiJ9." +
                        "eyJzdWIiOiIxMjM0NTY3ODkwIn0." +
                        "signature123";

        List<Detection> detections =
                detector.detect("$.authorization", text);

        assertEquals(1, detections.size());
    }

    @Test
    void ignoresOrdinaryDottedText() {
        List<Detection> detections =
                detector.detect(
                        "$.message",
                        "Visit api.example.com for documentation"
                );

        assertTrue(detections.isEmpty());
    }

    @Test
    void rejectsNullText() {
        assertThrows(
                NullPointerException.class,
                () -> detector.detect("$.token", null)
        );
    }
}