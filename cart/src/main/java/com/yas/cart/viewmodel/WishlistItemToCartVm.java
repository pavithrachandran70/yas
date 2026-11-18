package com.yas.cart.viewmodel;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record WishlistItemToCartVm(
    @NotNull Long wishlistItemId
) {
}
