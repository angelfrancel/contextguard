package com.contextguard.redaction;

import com.contextguard.detection.Detection;
import com.contextguard.detection.TextRange;
import com.contextguard.policy.DetectionType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgumentRedactorTest {

    private final ArgumentRedactor redactor = new ArgumentRedactor();

    @Test
    void redactsNestedFieldWithoutChangingOriginal() {
        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("user", "angel");
        credentials.put("password", "secret123");

        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("credentials", credentials);

        Detection detection = new Detection(
                DetectionType.SENSITIVE_JSON_FIELD,
                "$.credentials.password",
                Optional.empty()
        );

        Map<String, Object> result = redactor.redact(
                arguments,
                List.of(detection)
        );

        Map<?, ?> redactedCredentials =
                (Map<?, ?>) result.get("credentials");

        assertEquals("[REDACTED]", redactedCredentials.get("password"));
        assertEquals("angel", redactedCredentials.get("user"));
        assertEquals("secret123", credentials.get("password"));
        assertNotSame(credentials, redactedCredentials);
    }

    @Test
    void redactsTextInsideListWithoutChangingOriginal() {
        List<Object> messages = new ArrayList<>();
        messages.add("Call alice@example.com");

        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("messages", messages);

        Detection detection = new Detection(
                DetectionType.EMAIL,
                "$.messages[0]",
                Optional.of(new TextRange(5, 22))
        );

        Map<String, Object> result = redactor.redact(
                arguments,
                List.of(detection)
        );

        List<?> redactedMessages = (List<?>) result.get("messages");

        assertEquals("Call [REDACTED]", redactedMessages.get(0));
        assertEquals("Call alice@example.com", messages.get(0));
        assertNotSame(messages, redactedMessages);
    }

    @Test
    void rejectsDetectionPathThatDoesNotExist() {
        Detection detection = new Detection(
                DetectionType.SENSITIVE_JSON_FIELD,
                "$.missing",
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> redactor.redact(
                        Map.of("message", "hello"),
                        List.of(detection)
                )
        );
    }
}