package com.yas.customer.viewmodel.wishlist;

import lombok.Builder;

@Builder
public record WishlistItemVm(
    Long wishlistItemId,
    Long productId,
    String productName,
    String productSlug,
    String thumbnailUrl,
    Double price,
    Boolean isAvailable,
    String discontinuedMessage
) {
}
