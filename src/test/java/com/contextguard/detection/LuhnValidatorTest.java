package com.contextguard.detection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuhnValidatorTest {

    private final LuhnValidator validator = new LuhnValidator();

    @Test
    void acceptsValidVisaTestNumber() {
        assertTrue(validator.isValid("4111111111111111"));
    }

    @Test
    void acceptsValidMastercardTestNumber() {
        assertTrue(validator.isValid("5555555555554444"));
    }

    @Test
    void rejectsNumberThatFailsLuhn() {
        assertFalse(validator.isValid("4111111111111112"));
    }

    @Test
    void rejectsNumberThatIsTooShort() {
        assertFalse(validator.isValid("411111111111"));
    }

    @Test
    void rejectsNonDigitCharacters() {
        assertFalse(validator.isValid("411111111111111A"));
    }

    @Test
    void rejectsNullInput() {
        assertThrows(NullPointerException.class, () -> validator.isValid(null));
    }
}
