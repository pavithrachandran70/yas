package com.yas.customer.repository;

import com.yas.customer.model.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByCustomerIdOrderByCreatedOnDesc(String customerId);

    Optional<WishlistItem> findByIdAndCustomerId(Long id, String customerId);

    Optional<WishlistItem> findByCustomerIdAndProductId(String customerId, Long productId);
}
