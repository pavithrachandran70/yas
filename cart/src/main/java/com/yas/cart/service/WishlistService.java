package com.yas.cart.service;

import com.yas.cart.utils.Constants;
import com.yas.cart.viewmodel.WishlistItemDetailVm;
import com.yas.commonlibrary.config.ServiceUrlConfig;
import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.utils.AuthenticationUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class WishlistService extends AbstractCircuitBreakFallbackHandler {

    private final RestClient restClient;
    private final ServiceUrlConfig serviceUrlConfig;

    @Retry(name = "restApi")
    @CircuitBreaker(name = "restCircuitBreaker", fallbackMethod = "handleWishlistItemDetailFallback")
    public WishlistItemDetailVm getWishlistItemById(Long wishlistItemId) {
        final String jwt = AuthenticationUtils.extractJwt();
        final URI url = UriComponentsBuilder
            .fromUriString(serviceUrlConfig.customer())
            .path("/storefront/wishlist/items/{wishlistItemId}")
            .buildAndExpand(wishlistItemId)
            .toUri();

        try {
            return restClient.get()
                .uri(url)
                .headers(headers -> headers.setBearerAuth(jwt))
                .retrieve()
                .body(WishlistItemDetailVm.class);
        } catch (RestClientResponseException exception) {
            throw translateCustomerError(exception);
        }
    }

    protected WishlistItemDetailVm handleWishlistItemDetailFallback(Throwable throwable) throws Throwable {
        return handleTypedFallback(throwable);
    }

    private RuntimeException translateCustomerError(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        if (status == 404) {
            return new NotFoundException(Constants.ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        }
        if (status == 401 || status == 403) {
            return new AccessDeniedException(Constants.ErrorCode.WISHLIST_ITEM_ACCESS_DENIED);
        }
        return exception;
    }
}
