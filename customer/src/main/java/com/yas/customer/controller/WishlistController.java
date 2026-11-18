package com.yas.customer.controller;

import com.yas.commonlibrary.utils.AuthenticationUtils;
import com.yas.customer.service.WishlistService;
import com.yas.customer.viewmodel.wishlist.WishlistItemDetailVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemListVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemPostVm;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storefront/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    static final String PRODUCT_ADDED_MESSAGE = "Product added to wishlist";

    private final WishlistService wishlistService;

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> addToWishlist(@Valid @RequestBody WishlistItemPostVm request) {
        String customerId = AuthenticationUtils.extractUserId();
        wishlistService.addProductToWishlist(customerId, request.productId());
        return ResponseEntity.ok(Map.of("message", PRODUCT_ADDED_MESSAGE));
    }

    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WishlistItemListVm> getWishlist() {
        String customerId = AuthenticationUtils.extractUserId();
        return ResponseEntity.ok(wishlistService.getWishlistItems(customerId));
    }

    @DeleteMapping("/items/{wishlistItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long wishlistItemId) {
        String customerId = AuthenticationUtils.extractUserId();
        wishlistService.removeFromWishlist(customerId, wishlistItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items/{wishlistItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WishlistItemDetailVm> getWishlistItemDetail(@PathVariable Long wishlistItemId) {
        return ResponseEntity.ok(wishlistService.getWishlistItemDetail(wishlistItemId));
    }
}
