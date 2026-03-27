package com.example.shoppingguide.config;

import com.example.shoppingguide.domain.ProductOrder;
import com.example.shoppingguide.repository.ProductOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ProductOrderRepository orderRepository;
    private final VectorStore vectorStore;

    public DataSeeder(ProductOrderRepository orderRepository, VectorStore vectorStore) {
        this.orderRepository = orderRepository;
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🌱 [DataSeeder] 开始执行数据初始化...");

        // Seed MySQL Mock Orders
        if (orderRepository.count() == 0) {
            orderRepository.save(new ProductOrder("ORD123", "Alice", "iPhone 15 Pro", "SHIPPED"));
            orderRepository.save(new ProductOrder("ORD124", "Bob", "Samsung Galaxy S24", "PENDING"));
            log.info("🌱 [DataSeeder] MySQL 订单种子数据写入完成 (2 条记录)");
        } else {
            log.info("🌱 [DataSeeder] MySQL 中已存在订单数据，跳过写入");
        }

        // Seed Elasticsearch VectorStore (Products) using ETL Pipeline
        try {
            log.info("🌱 [DataSeeder] 开始向 Elasticsearch VectorStore 写入商品知识库...");
            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource("src/main/resources/products-catalog.txt");
            org.springframework.ai.reader.TextReader textReader = new org.springframework.ai.reader.TextReader(resource);
            textReader.getCustomMetadata().put("source", "products-catalog");
            
            org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new org.springframework.ai.transformer.splitter.TokenTextSplitter();
            List<Document> splitDocuments = splitter.apply(textReader.get());
            
            vectorStore.add(splitDocuments);
            log.info("🌱 [DataSeeder] Elasticsearch VectorStore 商品知识库写入完成，共 {} 个文档分片", splitDocuments.size());
        } catch (Exception e) {
            log.error("🌱 [DataSeeder] Elasticsearch VectorStore 写入失败，请检查 ES 是否运行: {}", e.getMessage());
        }

        log.info("🌱 [DataSeeder] 数据初始化流程结束");
    }
}
