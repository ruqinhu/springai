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

        // Seed Elasticsearch VectorStore (Products) using ETL Pipeline
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource("src/main/resources/products-catalog.txt");
            org.springframework.ai.reader.TextReader textReader = new org.springframework.ai.reader.TextReader(resource);
            textReader.getCustomMetadata().put("source", "products-catalog");
            
            org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new org.springframework.ai.transformer.splitter.TokenTextSplitter();
            List<Document> splitDocuments = splitter.apply(textReader.get());
            
            vectorStore.add(splitDocuments);
            System.out.println("✅ Seeded Mock Products into Elasticsearch VectorStore using TextReader and TokenTextSplitter. Total Chunks: " + splitDocuments.size());
        } catch (Exception e) {
            System.err.println("❌ Failed to seed Elasticsearch VectorStore. Is ES running? " + e.getMessage());
        }
    }
}
