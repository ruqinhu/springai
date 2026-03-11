package com.example.shoppingguide.repository;

import com.example.shoppingguide.domain.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, String> {
    Optional<ProductOrder> findByOrderIdAndCustomerName(String orderId, String customerName);
}
