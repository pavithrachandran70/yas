package com.yas.customer.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.WishlistItem;
import com.yas.customer.repository.WishlistItemRepository;
import com.yas.customer.utils.Constants;
import com.yas.customer.viewmodel.product.ProductThumbnailVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemDetailVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemListVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemVm;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    static final String DISCONTINUED_MESSAGE = "This product is no longer available";
    static final String UNABLE_TO_VERIFY_MESSAGE = "Unable to verify availability";
    private static final long PRODUCT_LOOKUP_TIMEOUT_SECONDS = 2L;
    private static final String DISCONTINUED_NAME = "Product no longer available";
    private static final String UNAVAILABLE_NAME = "Product information unavailable";

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductService productService;

    public void addProductToWishlist(String customerId, Long productId) {
        log.info("Adding product {} to wishlist for customer {}", productId, customerId);
        if (!productService.existsById(productId)) {
            throw new NotFoundException(Constants.ErrorCode.PRODUCT_NOT_FOUND, productId);
        }
        if (wishlistItemRepository.findByCustomerIdAndProductId(customerId, productId).isPresent()) {
            log.debug("Product {} is already on the wishlist for customer {}", productId, customerId);
            return;
        }
        try {
            WishlistItem item = WishlistItem.builder()
                .customerId(customerId)
                .productId(productId)
                .build();
            wishlistItemRepository.save(item);
        } catch (DataIntegrityViolationException exception) {
            log.debug(
                "Wishlist item already exists for customer {} and product {}",
                customerId,
                productId,
                exception
            );
        }
    }

    @Transactional(readOnly = true)
    public WishlistItemListVm getWishlistItems(String customerId) {
        log.info("Listing wishlist items for customer {}", customerId);
        List<WishlistItem> items = wishlistItemRepository.findByCustomerIdOrderByCreatedOnDesc(customerId);
        List<CompletableFuture<WishlistItemVm>> futures = items.stream()
            .map(this::fetchProductDetailsAsync)
            .toList();

        List<WishlistItemVm> itemVms = new ArrayList<>();
        for (int index = 0; index < futures.size(); index++) {
            itemVms.add(awaitProductDetails(items.get(index), futures.get(index)));
        }
        return new WishlistItemListVm(itemVms);
    }

    @Transactional
    public void removeFromWishlist(String customerId, Long wishlistItemId) {
        log.info("Removing wishlist item {} for customer {}", wishlistItemId, customerId);
        WishlistItem item = wishlistItemRepository.findByIdAndCustomerId(wishlistItemId, customerId)
            .orElseThrow(() -> new NotFoundException(Constants.ErrorCode.WISHLIST_ITEM_NOT_FOUND));
        wishlistItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public WishlistItemDetailVm getWishlistItemDetail(Long wishlistItemId) {
        log.info("Loading wishlist item {}", wishlistItemId);
        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
            .orElseThrow(() -> new NotFoundException(Constants.ErrorCode.WISHLIST_ITEM_NOT_FOUND));

        boolean available;
        try {
            ProductThumbnailVm product = productService.getProductById(item.getProductId());
            available = product != null;
        } catch (Exception exception) {
            log.error("Failed to verify availability for product {}", item.getProductId(), exception);
            available = false;
        }

        return WishlistItemDetailVm.builder()
            .wishlistItemId(item.getId())
            .customerId(item.getCustomerId())
            .productId(item.getProductId())
            .isAvailable(available)
            .build();
    }

    private CompletableFuture<WishlistItemVm> fetchProductDetailsAsync(WishlistItem item) {
        return CompletableFuture.supplyAsync(() -> toWishlistItemVm(item));
    }

    private WishlistItemVm awaitProductDetails(WishlistItem item, CompletableFuture<WishlistItemVm> future) {
        try {
            return future.get(PRODUCT_LOOKUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            log.error("Timed out loading product {} for wishlist item {}", item.getProductId(), item.getId());
            return unavailable(item);
        } catch (InterruptedException exception) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            log.error("Interrupted while loading product {} for wishlist item {}", item.getProductId(), item.getId());
            return unavailable(item);
        } catch (Exception exception) {
            log.error(
                "Failed to load product {} for wishlist item {}",
                item.getProductId(),
                item.getId(),
                exception
            );
            return unavailable(item);
        }
    }

    private WishlistItemVm toWishlistItemVm(WishlistItem item) {
        ProductThumbnailVm product = productService.getProductById(item.getProductId());
        if (product == null) {
            return discontinued(item);
        }
        return WishlistItemVm.builder()
            .wishlistItemId(item.getId())
            .productId(product.id())
            .productName(product.name())
            .productSlug(product.slug())
            .thumbnailUrl(product.thumbnailUrl())
            .price(product.price())
            .isAvailable(true)
            .discontinuedMessage(null)
            .build();
    }

    private WishlistItemVm discontinued(WishlistItem item) {
        return WishlistItemVm.builder()
            .wishlistItemId(item.getId())
            .productId(item.getProductId())
            .productName(DISCONTINUED_NAME)
            .productSlug("")
            .thumbnailUrl(null)
            .price(0.0)
            .isAvailable(false)
            .discontinuedMessage(DISCONTINUED_MESSAGE)
            .build();
    }

    private WishlistItemVm unavailable(WishlistItem item) {
        return WishlistItemVm.builder()
            .wishlistItemId(item.getId())
            .productId(item.getProductId())
            .productName(UNAVAILABLE_NAME)
            .productSlug("")
            .thumbnailUrl(null)
            .price(0.0)
            .isAvailable(false)
            .discontinuedMessage(UNABLE_TO_VERIFY_MESSAGE)
            .build();
    }
}
