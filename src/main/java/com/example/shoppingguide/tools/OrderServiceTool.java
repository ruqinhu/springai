package com.example.shoppingguide.tools;

import com.example.shoppingguide.domain.ProductOrder;
import com.example.shoppingguide.repository.ProductOrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class OrderServiceTool {

    private final ProductOrderRepository repository;

    public OrderServiceTool(ProductOrderRepository repository) {
        this.repository = repository;
    }

    public record OrderRequest(String orderId, String customerName) {}
    public record OrderResponse(String status, String details) {}

    @Bean
    @Description("Get the current status of a customer's order by their order ID and customer name")
    public Function<OrderRequest, OrderResponse> getOrderStatus() {
        return request -> {
            return repository.findByOrderIdAndCustomerName(request.orderId(), request.customerName())
                    .map(order -> new OrderResponse(order.getStatus(), 
                            "Order for " + order.getProductName() + " was placed on " + order.getOrderDate()))
                    .orElse(new OrderResponse("NOT_FOUND", "No matching order found for the given details."));
        };
    }
}
