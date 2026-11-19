package com.yas.rating.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RatingPostVmTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void star_WithinRange_HasNoViolations(int star) {
        Set<ConstraintViolation<RatingPostVm>> violations = validator.validate(rating(star));

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 6, 99})
    void star_OutsideRange_ReportsStarBetweenOneAndFive(int star) {
        Set<ConstraintViolation<RatingPostVm>> violations = validator.validate(rating(star));

        assertEquals(1, violations.size());
        ConstraintViolation<RatingPostVm> violation = violations.iterator().next();
        assertEquals("star", violation.getPropertyPath().toString());
        assertEquals("must be between 1 and 5", violation.getMessage());
    }

    private RatingPostVm rating(int star) {
        return RatingPostVm.builder()
                .content("comment")
                .star(star)
                .productId(1L)
                .productName("product")
                .build();
    }
}
