package com.example.shoppingguide.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 强制以 INFO 级别打印发送给大模型的内容和原始响应
 */
public class InfoLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    private static final Logger log = LoggerFactory.getLogger(InfoLoggerAdvisor.class);

    public InfoLoggerAdvisor() {
        log.info("🔍 [InfoLoggerAdvisor] 实例已创建，正在进入 ChatClient 增强链路...");
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        log.info("▶️ [LLM Request - Sync] 发送给大模型的完整 Payload:\n{}", formatRequest(advisedRequest));
        AdvisedResponse response = chain.nextAroundCall(advisedRequest);
        log.info("◀️ [LLM Response - Sync] 大模型的原始响应:\n{}", formatResponse(response));
        return response;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        log.info("▶️ [LLM Request - Stream] 发送给大模型的完整 Payload:\n{}", formatRequest(advisedRequest));
        return chain.nextAroundStream(advisedRequest)
                .doOnNext(response -> {
                    // 只打印最终生成的块中的完整或追加内容（流的每一个块）
                    if (response != null && response.response() != null && response.response().getResult() != null && response.response().getResult().getOutput() != null) {
                        String content = response.response().getResult().getOutput().getText();
                        if (content != null && !content.isEmpty()) {
                            log.info("◀️ [LLM Response - Stream Chunk]: {}", content);
                        }
                    }
                });
    }

    @Override
    public String getName() {
        return "InfoLoggerAdvisor";
    }

    @Override
    public int getOrder() {
        // 设为最高优先级（最后执行），确保能打印经过其他 Advisor（比如 RAG、History）修改后的最终 Prompt
        return Integer.MAX_VALUE;
    }

    private String formatRequest(AdvisedRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("System Text: ").append(request.systemText()).append("\n");
        sb.append("System Params: ").append(request.systemParams()).append("\n");
        sb.append("User Text: ").append(request.userText()).append("\n");
        sb.append("User Params: ").append(request.userParams()).append("\n");
        sb.append("Messages: [\n");
        request.messages().forEach(m -> sb.append("  ").append(m.getMessageType()).append(": ").append(m.getText()).append("\n"));
        sb.append("]\n");
        return sb.toString();
    }

    private String formatResponse(AdvisedResponse response) {
        if (response == null || response.response() == null || response.response().getResult() == null || response.response().getResult().getOutput() == null) {
            return "NULL";
        }
        return response.response().getResult().getOutput().getText();
    }
}

