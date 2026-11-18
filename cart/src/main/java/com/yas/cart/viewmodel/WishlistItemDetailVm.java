package com.yas.cart.viewmodel;

import lombok.Builder;

@Builder
public record WishlistItemDetailVm(
    Long wishlistItemId,
    String customerId,
    Long productId,
    Boolean isAvailable
) {
}
