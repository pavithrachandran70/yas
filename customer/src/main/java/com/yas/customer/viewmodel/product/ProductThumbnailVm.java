package com.yas.customer.viewmodel.product;

public record ProductThumbnailVm(
    Long id,
    String name,
    String slug,
    String thumbnailUrl,
    Double price
) {
}
