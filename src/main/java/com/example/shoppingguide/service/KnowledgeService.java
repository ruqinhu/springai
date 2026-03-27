package com.example.shoppingguide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeService {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    private final VectorStore knowledgeVectorStore;
    private final TokenTextSplitter splitter;

    public KnowledgeService(@Qualifier("knowledgeVectorStore") VectorStore knowledgeVectorStore) {
        this.knowledgeVectorStore = knowledgeVectorStore;
        this.splitter = new TokenTextSplitter();
    }

    /**
     * 批量上传文件，经 Tika 读取 → 分片 → 向量化 → 存入 ES knowledge 索引
     */
    public Map<String, Object> uploadAndVectorize(MultipartFile[] files) {
        log.info("📂 [KnowledgeService] 开始处理 {} 个文件的上传与向量化", files.length);

        int totalChunks = 0;
        List<String> successFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            try {
                log.info("📂 [KnowledgeService] 正在处理文件: {} (大小: {} bytes)", filename, file.getSize());

                // 1. 用 Tika 读取文件内容
                var resource = new InputStreamResource(file.getInputStream());
                var reader = new TikaDocumentReader(resource);
                List<Document> documents = reader.get();

                // 2. 为每个文档注入来源元数据
                documents.forEach(doc -> doc.getMetadata().put("source", filename));

                // 3. Token 分片
                List<Document> chunks = splitter.apply(documents);

                // 4. 写入 VectorStore
                knowledgeVectorStore.add(chunks);

                totalChunks += chunks.size();
                successFiles.add(filename + " (" + chunks.size() + " chunks)");
                log.info("✅ [KnowledgeService] 文件 {} 处理完成，分为 {} 个向量分片", filename, chunks.size());

            } catch (Exception e) {
                failedFiles.add(filename + " (" + e.getMessage() + ")");
                log.error("❌ [KnowledgeService] 文件 {} 处理失败: {}", filename, e.getMessage(), e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalFiles", files.length);
        result.put("totalChunks", totalChunks);
        result.put("success", successFiles);
        result.put("failed", failedFiles);

        log.info("📂 [KnowledgeService] 批量上传完成。成功: {}, 失败: {}, 总分片: {}",
                successFiles.size(), failedFiles.size(), totalChunks);

        return result;
    }

    /**
     * 向量相似度搜索（测试用）
     */
    public List<Map<String, Object>> searchSimilar(String query, int topK) {
        log.info("🔎 [KnowledgeService] 执行知识库相似度搜索 - query: {}, topK: {}", query, topK);
        List<Document> results = knowledgeVectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build()
        );

        List<Map<String, Object>> response = new ArrayList<>();
        for (Document doc : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", doc.getText());
            item.put("metadata", doc.getMetadata());
            response.add(item);
        }

        log.info("🔎 [KnowledgeService] 搜索完成，返回 {} 条结果", results.size());
        return response;
    }
}
