package com.example.shoppingguide.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_orders")
public class ProductOrder {

    @Id
    private String orderId;

    private String customerName;
    private String productName;
    private String status; // PENDING, SHIPPED, DELIVERED
    private LocalDateTime orderDate;

    public ProductOrder() {}

    public ProductOrder(String orderId, String customerName, String productName, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.productName = productName;
        this.status = status;
        this.orderDate = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
}
