package com.example.shoppingguide.config;

import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;

/**
 * 手动配置 Elasticsearch VectorStore
 * 因为一旦在配置类中声明了 VectorStore Bean，Spring Boot 的 AutoConfig 就会退避。
 * 所以我们必须将原本用于导购的 products 索引也通过 Bean 提供，并作为 @Primary。
 */
@Configuration
public class VectorStoreConfig {
    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Bean
    @Primary
    public ElasticsearchVectorStore productsVectorStore(RestClient restClient, 
                                                        EmbeddingModel embeddingModel,
                                                        @Value("${spring.ai.vectorstore.elasticsearch.dimensions:1024}") int dimensions) {
        log.info("📚 [VectorStoreConfig] 初始化主 VectorStore (导购)，索引名: products");
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName("products");
        options.setDimensions(dimensions);

        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)
                .initializeSchema(true)
                .build();
    }

    @Bean
    @Qualifier("knowledgeVectorStore")
    public ElasticsearchVectorStore knowledgeVectorStore(RestClient restClient, 
                                                         EmbeddingModel embeddingModel,
                                                         @Value("${spring.ai.vectorstore.elasticsearch.dimensions:1024}") int dimensions) {
        log.info("📚 [VectorStoreConfig] 初始化附加 VectorStore (知识库)，索引名: knowledge");
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName("knowledge");
        options.setDimensions(dimensions);

        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)
                .initializeSchema(true)
                .build();
    }
}
