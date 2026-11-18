package com.yas.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.cart.viewmodel.WishlistItemDetailVm;
import com.yas.commonlibrary.config.ServiceUrlConfig;
import com.yas.commonlibrary.exception.NotFoundException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    private static final Long WISHLIST_ITEM_ID_SAMPLE = 1L;
    private static final String CUSTOMER_ID_SAMPLE = "customer-uuid";
    private static final Long PRODUCT_ID_SAMPLE = 123L;

    @Mock
    private RestClient restClient;

    @Mock
    private ServiceUrlConfig serviceUrlConfig;

    @InjectMocks
    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        mockJwtAuthentication();
    }

    @Test
    void testGetWishlistItemById_Success() {
        WishlistItemDetailVm expectedVm = WishlistItemDetailVm.builder()
            .wishlistItemId(WISHLIST_ITEM_ID_SAMPLE)
            .customerId(CUSTOMER_ID_SAMPLE)
            .productId(PRODUCT_ID_SAMPLE)
            .isAvailable(true)
            .build();

        when(serviceUrlConfig.customer()).thenReturn("http://customer-service");
        RestClient.ResponseSpec responseSpec = stubRestClient();
        when(responseSpec.body(WishlistItemDetailVm.class)).thenReturn(expectedVm);

        WishlistItemDetailVm result = wishlistService.getWishlistItemById(WISHLIST_ITEM_ID_SAMPLE);

        assertNotNull(result);
        assertEquals(WISHLIST_ITEM_ID_SAMPLE, result.wishlistItemId());
        assertEquals(CUSTOMER_ID_SAMPLE, result.customerId());
        assertEquals(PRODUCT_ID_SAMPLE, result.productId());
        assertTrue(result.isAvailable());
    }

    @Test
    void testGetWishlistItemById_NotFound() {
        when(serviceUrlConfig.customer()).thenReturn("http://customer-service");
        RestClient.ResponseSpec responseSpec = stubRestClient();
        when(responseSpec.body(WishlistItemDetailVm.class))
            .thenThrow(new NotFoundException("Wishlist item not found"));

        assertThrows(NotFoundException.class, () -> wishlistService.getWishlistItemById(WISHLIST_ITEM_ID_SAMPLE));
    }

    @Test
    void testGetWishlistItemById_CustomerReturnsNotFound() {
        when(serviceUrlConfig.customer()).thenReturn("http://customer-service");
        RestClient.ResponseSpec responseSpec = stubRestClient();
        when(responseSpec.body(WishlistItemDetailVm.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertThrows(NotFoundException.class, () -> wishlistService.getWishlistItemById(WISHLIST_ITEM_ID_SAMPLE));
    }

    @Test
    void testGetWishlistItemById_Timeout() {
        when(serviceUrlConfig.customer()).thenReturn("http://customer-service");
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = stubRequest();
        when(requestHeadersUriSpec.retrieve()).thenThrow(new RuntimeException("Timeout"));

        assertThrows(RuntimeException.class, () -> wishlistService.getWishlistItemById(WISHLIST_ITEM_ID_SAMPLE));
    }

    @SuppressWarnings("unchecked")
    private RestClient.ResponseSpec stubRestClient() {
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = stubRequest();
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        return responseSpec;
    }

    @SuppressWarnings("unchecked")
    private RestClient.RequestHeadersUriSpec stubRequest() {
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
        return requestHeadersUriSpec;
    }

    private void mockJwtAuthentication() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mock-jwt-token");

        JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(jwtToken);
        SecurityContextHolder.setContext(securityContext);
    }
}
