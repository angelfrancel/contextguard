package com.contextguard.detection;

import com.contextguard.policy.DetectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuthenticationTokenDetector
        implements TextDetector {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile(
                    "(?i:\\bBearer[\\t ]+)" +
                            "(?<bearer>[A-Za-z0-9._~+/-]+={0,2})" +
                            "|" +
                            "(?<![A-Za-z0-9_-])" +
                            "(?<jwt>" +
                            "eyJ[A-Za-z0-9_-]*\\." +
                            "eyJ[A-Za-z0-9_-]*\\." +
                            "[A-Za-z0-9_-]+" +
                            ")" +
                            "(?![A-Za-z0-9_-])"
            );

    @Override
    public List<Detection> detect(String jsonPath, String text) {
        Objects.requireNonNull(
                jsonPath,
                "jsonPath must not be null"
        );
        Objects.requireNonNull(
                text,
                "text must not be null"
        );

        if (jsonPath.isBlank()) {
            throw new IllegalArgumentException(
                    "jsonPath must not be blank"
            );
        }

        Matcher matcher = TOKEN_PATTERN.matcher(text);
        List<Detection> detections = new ArrayList<>();

        while (matcher.find()) {
            String matchedGroup =
                    matcher.group("bearer") != null
                            ? "bearer"
                            : "jwt";

            detections.add(
                    new Detection(
                            DetectionType.AUTHENTICATION_TOKEN,
                            jsonPath,
                            Optional.of(
                                    new TextRange(
                                            matcher.start(matchedGroup),
                                            matcher.end(matchedGroup)
                                    )
                            )
                    )
            );
        }

        return List.copyOf(detections);
    }
}