package com.example.shoppingguide.config;

import com.example.shoppingguide.domain.ProductOrder;
import com.example.shoppingguide.repository.ProductOrderRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductOrderRepository orderRepository;
    private final VectorStore vectorStore;

    public DataSeeder(ProductOrderRepository orderRepository, VectorStore vectorStore) {
        this.orderRepository = orderRepository;
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed MySQL Mock Orders
        if (orderRepository.count() == 0) {
            orderRepository.save(new ProductOrder("ORD123", "Alice", "iPhone 15 Pro", "SHIPPED"));
            orderRepository.save(new ProductOrder("ORD124", "Bob", "Samsung Galaxy S24", "PENDING"));
            System.out.println("✅ Seeded Mock Orders into MySQL");
        }

        // Seed Elasticsearch VectorStore (Products)
        try {
            List<Document> products = List.of(
                    new Document("iPhone 15 Pro features a titanium design, A17 Pro chip, and a 48MP main camera. Price: $999.", 
                            Map.of("name", "iPhone 15 Pro", "category", "Smartphone")),
                    new Document("Samsung Galaxy S24 Ultra comes with Snapdragon 8 Gen 3, S-Pen, and a flat 6.8 inch display. Price: $1199.", 
                            Map.of("name", "Samsung Galaxy S24 Ultra", "category", "Smartphone")),
                    new Document("Sony WH-1000XM5 are industry-leading noise canceling headphones with 30-hour battery life. Price: $399.", 
                            Map.of("name", "Sony WH-1000XM5", "category", "Audio"))
            );
            vectorStore.add(products);
            System.out.println("✅ Seeded Mock Products into Elasticsearch VectorStore");
        } catch (Exception e) {
            System.err.println("❌ Failed to seed Elasticsearch VectorStore. Is ES running? " + e.getMessage());
        }
    }
}
