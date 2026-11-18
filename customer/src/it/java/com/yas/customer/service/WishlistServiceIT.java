package com.yas.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.customer.model.WishlistItem;
import com.yas.customer.repository.WishlistItemRepository;
import com.yas.customer.viewmodel.product.ProductThumbnailVm;
import com.yas.customer.viewmodel.wishlist.WishlistItemListVm;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class WishlistServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16");

    private static final String CUSTOMER_ID = "customer-1";
    private static final Long PRODUCT_ID = 123L;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private ProductService productService;

    @BeforeEach
    void clean() {
        wishlistItemRepository.deleteAll();
    }

    @AfterEach
    void clearTestData() {
        wishlistItemRepository.deleteAll();
    }

    @Test
    void testAddProductToWishlist_PersistsAcrossSessions() {
        Long savedId = transactionTemplate.execute(status -> {
            WishlistItem saved = wishlistItemRepository.saveAndFlush(WishlistItem.builder()
                .customerId(CUSTOMER_ID)
                .productId(PRODUCT_ID)
                .build());
            entityManager.flush();
            entityManager.clear();
            return saved.getId();
        });

        WishlistItem reloaded = wishlistItemRepository.findById(savedId).orElseThrow();
        assertThat(reloaded.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(reloaded.getProductId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void testRemoveWishlistItem_DeletesFromDatabase() {
        WishlistItem saved = wishlistItemRepository.saveAndFlush(WishlistItem.builder()
            .customerId(CUSTOMER_ID)
            .productId(PRODUCT_ID)
            .build());

        wishlistItemRepository.delete(saved);
        wishlistItemRepository.flush();

        assertThat(wishlistItemRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void testAddProductToWishlist_NoDuplicates() {
        wishlistItemRepository.saveAndFlush(WishlistItem.builder()
            .customerId(CUSTOMER_ID)
            .productId(PRODUCT_ID)
            .build());

        assertThrows(DataIntegrityViolationException.class, () -> wishlistItemRepository.saveAndFlush(
            WishlistItem.builder()
                .customerId(CUSTOMER_ID)
                .productId(PRODUCT_ID)
                .build()
        ));
    }

    @Test
    void testGetWishlistItems_CallsProductService() {
        WishlistItem saved = wishlistItemRepository.saveAndFlush(WishlistItem.builder()
            .customerId(CUSTOMER_ID)
            .productId(PRODUCT_ID)
            .build());
        when(productService.getProductById(PRODUCT_ID)).thenReturn(new ProductThumbnailVm(
            PRODUCT_ID,
            "Example Product",
            "example-product",
            "https://example.com/media/thumbnail.jpg",
            29.99
        ));

        WishlistItemListVm result = wishlistService.getWishlistItems(CUSTOMER_ID);

        verify(productService).getProductById(PRODUCT_ID);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().wishlistItemId()).isEqualTo(saved.getId());
        assertThat(result.items().getFirst().isAvailable()).isTrue();
    }
}
