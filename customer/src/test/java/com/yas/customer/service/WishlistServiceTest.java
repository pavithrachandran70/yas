package com.yas.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.WishlistItem;
import com.yas.customer.repository.WishlistItemRepository;
import com.yas.customer.viewmodel.product.ProductThumbnailVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemDetailVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemListVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    private static final String CUSTOMER_ID = "customer-1";
    private static final Long PRODUCT_ID = 123L;
    private static final Long WISHLIST_ITEM_ID = 1L;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void testAddProductToWishlist_Success() {
        when(productService.existsById(PRODUCT_ID)).thenReturn(true);
        when(wishlistItemRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
            .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> wishlistService.addProductToWishlist(CUSTOMER_ID, PRODUCT_ID));

        verify(productService).existsById(PRODUCT_ID);
        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }

    @Test
    void testAddProductToWishlist_Idempotent() {
        when(productService.existsById(PRODUCT_ID)).thenReturn(true);
        when(wishlistItemRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
            .thenReturn(Optional.of(existingItem()));

        wishlistService.addProductToWishlist(CUSTOMER_ID, PRODUCT_ID);

        verify(wishlistItemRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void testAddProductToWishlist_DuplicateConstraintIsIdempotent() {
        when(productService.existsById(PRODUCT_ID)).thenReturn(true);
        when(wishlistItemRepository.findByCustomerIdAndProductId(CUSTOMER_ID, PRODUCT_ID))
            .thenReturn(Optional.empty());
        when(wishlistItemRepository.save(any(WishlistItem.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertDoesNotThrow(() -> wishlistService.addProductToWishlist(CUSTOMER_ID, PRODUCT_ID));
    }

    @Test
    void testAddProductToWishlist_ProductNotFound() {
        when(productService.existsById(PRODUCT_ID)).thenReturn(false);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> wishlistService.addProductToWishlist(CUSTOMER_ID, PRODUCT_ID)
        );

        assertThat(exception.getMessage()).isEqualTo("Product with ID 123 not found");
        verify(wishlistItemRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void testGetWishlistItems_Success() {
        when(wishlistItemRepository.findByCustomerIdOrderByCreatedOnDesc(CUSTOMER_ID))
            .thenReturn(List.of(existingItem()));
        when(productService.getProductById(PRODUCT_ID)).thenReturn(productThumbnail());

        WishlistItemListVm result = wishlistService.getWishlistItems(CUSTOMER_ID);

        assertThat(result.items()).hasSize(1);
        WishlistItemVm item = result.items().getFirst();
        assertThat(item.wishlistItemId()).isEqualTo(WISHLIST_ITEM_ID);
        assertThat(item.productId()).isEqualTo(PRODUCT_ID);
        assertThat(item.productName()).isEqualTo("Example Product");
        assertThat(item.productSlug()).isEqualTo("example-product");
        assertThat(item.thumbnailUrl()).isEqualTo("https://example.com/media/thumbnail.jpg");
        assertThat(item.price()).isEqualTo(29.99);
        assertThat(item.isAvailable()).isTrue();
        assertThat(item.discontinuedMessage()).isNull();
    }

    @Test
    void testGetWishlistItems_ProductUnavailable() {
        when(wishlistItemRepository.findByCustomerIdOrderByCreatedOnDesc(CUSTOMER_ID))
            .thenReturn(List.of(existingItem()));
        when(productService.getProductById(PRODUCT_ID)).thenReturn(null);

        WishlistItemListVm result = wishlistService.getWishlistItems(CUSTOMER_ID);

        assertThat(result.items()).hasSize(1);
        WishlistItemVm item = result.items().getFirst();
        assertThat(item.wishlistItemId()).isEqualTo(WISHLIST_ITEM_ID);
        assertThat(item.productId()).isEqualTo(PRODUCT_ID);
        assertThat(item.isAvailable()).isFalse();
        assertThat(item.discontinuedMessage()).isEqualTo(WishlistService.DISCONTINUED_MESSAGE);
    }

    @Test
    void testGetWishlistItems_ProductServiceError() {
        when(wishlistItemRepository.findByCustomerIdOrderByCreatedOnDesc(CUSTOMER_ID))
            .thenReturn(List.of(existingItem()));
        when(productService.getProductById(PRODUCT_ID)).thenThrow(new IllegalStateException("product down"));

        WishlistItemListVm result = wishlistService.getWishlistItems(CUSTOMER_ID);

        WishlistItemVm item = result.items().getFirst();
        assertThat(item.wishlistItemId()).isEqualTo(WISHLIST_ITEM_ID);
        assertThat(item.productId()).isEqualTo(PRODUCT_ID);
        assertThat(item.isAvailable()).isFalse();
        assertThat(item.discontinuedMessage()).isEqualTo(WishlistService.UNABLE_TO_VERIFY_MESSAGE);
    }

    @Test
    void testRemoveWishlistItem_Success() {
        WishlistItem item = existingItem();
        when(wishlistItemRepository.findByIdAndCustomerId(WISHLIST_ITEM_ID, CUSTOMER_ID))
            .thenReturn(Optional.of(item));

        wishlistService.removeFromWishlist(CUSTOMER_ID, WISHLIST_ITEM_ID);

        verify(wishlistItemRepository).delete(item);
    }

    @Test
    void testRemoveWishlistItem_NotFound() {
        when(wishlistItemRepository.findByIdAndCustomerId(WISHLIST_ITEM_ID, CUSTOMER_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> wishlistService.removeFromWishlist(CUSTOMER_ID, WISHLIST_ITEM_ID)
        );

        assertThat(exception.getMessage()).isEqualTo("Wishlist item not found");
        verify(wishlistItemRepository, never()).delete(any(WishlistItem.class));
    }

    @Test
    void testGetWishlistItemInternal_Success() {
        when(wishlistItemRepository.findById(WISHLIST_ITEM_ID)).thenReturn(Optional.of(existingItem()));
        when(productService.getProductById(PRODUCT_ID)).thenReturn(productThumbnail());

        WishlistItemDetailVm result = wishlistService.getWishlistItemDetail(WISHLIST_ITEM_ID);

        assertThat(result.wishlistItemId()).isEqualTo(WISHLIST_ITEM_ID);
        assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void testGetWishlistItemInternal_NotFound() {
        when(wishlistItemRepository.findById(WISHLIST_ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> wishlistService.getWishlistItemDetail(WISHLIST_ITEM_ID));
    }

    private WishlistItem existingItem() {
        return WishlistItem.builder()
            .id(WISHLIST_ITEM_ID)
            .customerId(CUSTOMER_ID)
            .productId(PRODUCT_ID)
            .build();
    }

    private ProductThumbnailVm productThumbnail() {
        return new ProductThumbnailVm(
            PRODUCT_ID,
            "Example Product",
            "example-product",
            "https://example.com/media/thumbnail.jpg",
            29.99
        );
    }
}
