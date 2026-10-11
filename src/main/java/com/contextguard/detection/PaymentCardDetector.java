package com.contextguard.detection;

import com.contextguard.policy.DetectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PaymentCardDetector implements TextDetector {

    private static final Pattern CARD_CANDIDATE_PATTERN =
            Pattern.compile(
                    "(?<!\\d)(?<!\\d[ -])" +
                            "(?:\\d[ -]?){12,18}\\d" +
                            "(?![ -]?\\d)"
            );

    private final LuhnValidator luhnValidator;

    public PaymentCardDetector(LuhnValidator luhnValidator) {
        this.luhnValidator = Objects.requireNonNull(
                luhnValidator,
                "luhnValidator must not be null"
        );
    }

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

        Matcher matcher = CARD_CANDIDATE_PATTERN.matcher(text);

        List<Detection> detections = new ArrayList<>();

        while (matcher.find()) {
            String candidate = matcher.group();

            String digits = candidate
                    .replace(" ", "")
                    .replace("-", "");

            if (!luhnValidator.isValid(digits)) {
                continue;
            }

            detections.add(
                    new Detection(
                            DetectionType.PAYMENT_CARD,
                            jsonPath,
                            Optional.of(
                                    new TextRange(
                                            matcher.start(),
                                            matcher.end()
                                    )
                            )
                    )
            );
        }

        return List.copyOf(detections);
    }
}