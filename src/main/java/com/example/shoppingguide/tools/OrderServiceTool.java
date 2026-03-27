package com.example.shoppingguide.tools;

import com.example.shoppingguide.repository.ProductOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class OrderServiceTool {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceTool.class);

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
            log.info("🔧 [Tool:getOrderStatus] 被大模型函数调用触发 - orderId: {}, customerName: {}", request.orderId(), request.customerName());
            return repository.findByOrderIdAndCustomerName(request.orderId(), request.customerName())
                    .map(order -> {
                        log.info("🔧 [Tool:getOrderStatus] 查询到订单 - Status: {}, Product: {}", order.getStatus(), order.getProductName());
                        return new OrderResponse(order.getStatus(),
                                "Order for " + order.getProductName() + " was placed on " + order.getOrderDate());
                    })
                    .orElseGet(() -> {
                        log.warn("🔧 [Tool:getOrderStatus] 未找到匹配订单 - orderId: {}, customerName: {}", request.orderId(), request.customerName());
                        return new OrderResponse("NOT_FOUND", "No matching order found for the given details.");
                    });
        };
    }
}
