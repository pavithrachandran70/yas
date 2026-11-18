package com.yas.customer.viewmodel.wishlist;

import lombok.Builder;

@Builder
public record WishlistItemDetailVm(
    Long wishlistItemId,
    String customerId,
    Long productId,
    Boolean isAvailable
) {
}
