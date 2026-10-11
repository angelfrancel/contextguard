package com.contextguard.detection;

import java.util.Objects;

public final class LuhnValidator {

    public boolean isValid(String digits) {
        Objects.requireNonNull(
                digits,
                "digits must not be null"
        );

        if (!(digits.length() >= 13 && digits.length() <= 19)) {
            return false;
        }

        int sum = 0;
        boolean doubleCurrent = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            char character = digits.charAt(i);

            if (character < '0' || character > '9') {
                return false;
            }

            int digit = character - '0';

            if (doubleCurrent) {
                digit *= 2;

                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleCurrent = !doubleCurrent;
        }

        return sum % 10 == 0;
    }
}