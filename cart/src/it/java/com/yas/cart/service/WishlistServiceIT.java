package com.yas.cart.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ComponentScan(basePackages = {"com.yas.commonlibrary"})
class WishlistServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16");

    @MockitoSpyBean
    private WishlistService wishlistService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void testGetWishlistItemById_RealCall() throws Throwable {
        circuitBreakerRegistry.circuitBreaker("restCircuitBreaker").transitionToOpenState();
        assertThrows(CallNotPermittedException.class, () -> wishlistService.getWishlistItemById(1L));
        verify(wishlistService, atLeastOnce()).handleWishlistItemDetailFallback(any());
    }
}
