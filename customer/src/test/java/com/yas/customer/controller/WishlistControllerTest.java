package com.yas.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.commonlibrary.exception.ApiExceptionHandler;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.service.WishlistService;
import com.yas.customer.utils.Constants;
import com.yas.customer.viewmodel.wishlist.WishlistItemDetailVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemListVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemVm;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = WishlistController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class WishlistControllerTest {

    private static final String BASE_URL = "/storefront/wishlist/items";
    private static final String CUSTOMER_ID = "customer-1";

    @MockitoBean
    private WishlistService wishlistService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject(CUSTOMER_ID)
            .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .expiresAt(Instant.parse("2026-01-01T01:00:00Z"))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddProductToWishlist_ReturnsOk() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":123}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(WishlistController.PRODUCT_ADDED_MESSAGE));

        verify(wishlistService).addProductToWishlist(CUSTOMER_ID, 123L);
    }

    @Test
    void testGetWishlistItems_ReturnsOk() throws Exception {
        WishlistItemListVm response = new WishlistItemListVm(List.of(
            WishlistItemVm.builder()
                .wishlistItemId(1L)
                .productId(123L)
                .productName("Example Product")
                .productSlug("example-product")
                .thumbnailUrl("https://example.com/media/thumbnail.jpg")
                .price(29.99)
                .isAvailable(true)
                .discontinuedMessage(null)
                .build()
        ));
        when(wishlistService.getWishlistItems(CUSTOMER_ID)).thenReturn(response);

        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].wishlistItemId").value(1))
            .andExpect(jsonPath("$.items[0].productId").value(123))
            .andExpect(jsonPath("$.items[0].productName").value("Example Product"))
            .andExpect(jsonPath("$.items[0].isAvailable").value(true));
    }

    @Test
    void testGetWishlistItemDetail_ReturnsOk() throws Exception {
        WishlistItemDetailVm response = WishlistItemDetailVm.builder()
            .wishlistItemId(1L)
            .customerId(CUSTOMER_ID)
            .productId(123L)
            .isAvailable(true)
            .build();
        when(wishlistService.getWishlistItemDetail(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{wishlistItemId}", 1L).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wishlistItemId").value(1))
            .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
            .andExpect(jsonPath("$.productId").value(123))
            .andExpect(jsonPath("$.isAvailable").value(true));
    }

    @Test
    void testRemoveWishlistItem_ReturnsNoContent() throws Exception {
        doNothing().when(wishlistService).removeFromWishlist(CUSTOMER_ID, 1L);

        mockMvc.perform(delete(BASE_URL + "/{wishlistItemId}", 1L))
            .andExpect(status().isNoContent());

        verify(wishlistService).removeFromWishlist(CUSTOMER_ID, 1L);
    }

    @Test
    void testRemoveWishlistItem_NotFound() throws Exception {
        doThrow(new NotFoundException(Constants.ErrorCode.WISHLIST_ITEM_NOT_FOUND))
            .when(wishlistService).removeFromWishlist(eq(CUSTOMER_ID), eq(9L));

        mockMvc.perform(delete(BASE_URL + "/{wishlistItemId}", 9L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Wishlist item not found"));
    }

    @Test
    void testAddProductToWishlist_MissingProductId() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verify(wishlistService, never()).addProductToWishlist(any(), any());
    }
}
