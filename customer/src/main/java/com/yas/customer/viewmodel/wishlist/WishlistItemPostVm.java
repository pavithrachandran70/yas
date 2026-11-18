package com.yas.customer.viewmodel.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record WishlistItemPostVm(
    @NotNull(message = "Product ID is required") Long productId
) {
}
