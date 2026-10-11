package com.contextguard.detection;

import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetectionPipelineTest {

    private final DetectionPipeline pipeline =
            new DetectionPipeline(
                    List.of(
                            new EmailDetector(),
                            new PhilippineMobileDetector(),
                            new PaymentCardDetector(
                                    new LuhnValidator()
                            ),
                            new AuthenticationTokenDetector()
                    ),
                    new SensitiveJsonFieldDetector()
            );

    @Test
    void detectsAllRequiredTypesAcrossNestedArguments() {
        Map<String, Object> arguments =
                Map.of(
                        "message",
                        "Email angel@example.com or call 09171234567",
                        "payment",
                        "Use test card 4111111111111111",
                        "headers",
                        Map.of(
                                "authorization",
                                "Bearer fake-token-123"
                        )
                );

        List<Detection> detections =
                pipeline.detect(arguments);

        Set<DetectionType> detectedTypes =
                detections.stream()
                        .map(Detection::type)
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        DetectionType.EMAIL,
                        DetectionType.PHILIPPINE_MOBILE_NUMBER,
                        DetectionType.PAYMENT_CARD,
                        DetectionType.AUTHENTICATION_TOKEN,
                        DetectionType.SENSITIVE_JSON_FIELD
                ),
                detectedTypes
        );
    }

    @Test
    void reportsPathInsideArray() {
        Map<String, Object> arguments =
                Map.of(
                        "recipients",
                        List.of(
                                Map.of(
                                        "contact",
                                        "angel@example.com"
                                )
                        )
                );

        List<Detection> detections =
                pipeline.detect(arguments);

        assertEquals(1, detections.size());
        assertEquals(
                "$.recipients[0].contact",
                detections.getFirst().jsonPath()
        );
    }

    @Test
    void returnsEmptyListWhenNothingIsDetected() {
        Map<String, Object> arguments =
                Map.of(
                        "message", "Hello world",
                        "count", 3,
                        "enabled", true
                );

        List<Detection> detections =
                pipeline.detect(arguments);

        assertTrue(detections.isEmpty());
    }

    @Test
    void returnsImmutableDetectionList() {
        List<Detection> detections =
                pipeline.detect(
                        Map.of(
                                "email",
                                "angel@example.com"
                        )
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> detections.add(
                        new Detection(
                                DetectionType.EMAIL,
                                "$.another",
                                Optional.of(
                                        new TextRange(0, 1)
                                )
                        )
                )
        );
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(
                NullPointerException.class,
                () -> pipeline.detect(null)
        );
    }
}