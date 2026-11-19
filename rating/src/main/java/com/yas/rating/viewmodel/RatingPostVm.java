package com.yas.rating.viewmodel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record RatingPostVm(
        String content,
        @Min(value = 1, message = "must be between 1 and 5")
        @Max(value = 5, message = "must be between 1 and 5")
        int star,
        Long productId,
        String productName
) {

}

