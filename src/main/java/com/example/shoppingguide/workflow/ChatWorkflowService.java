package com.example.shoppingguide.workflow;

import com.example.shoppingguide.agent.OrderAgent;
import com.example.shoppingguide.agent.RouterAgent;
import com.example.shoppingguide.agent.ShoppingAgent;
import com.example.shoppingguide.domain.IntentType;
import com.example.shoppingguide.event.ChatLogEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(ChatWorkflowService.class);

    private final RouterAgent routerAgent;
    private final OrderAgent orderAgent;
    private final ShoppingAgent shoppingAgent;
    private final ApplicationEventPublisher eventPublisher;

    private StateGraph.CompiledGraph chatGraph;

    public ChatWorkflowService(RouterAgent routerAgent, OrderAgent orderAgent,
                               ShoppingAgent shoppingAgent, ApplicationEventPublisher eventPublisher) {
        this.routerAgent = routerAgent;
        this.orderAgent = orderAgent;
        this.shoppingAgent = shoppingAgent;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        log.info("⚙️ [ChatWorkflowService] 正在初始化 StateGraph 工作流...");

        StateGraph graph = new StateGraph();

        // 1. Define Nodes
        graph.addNode("router", state -> {
            state.setIntent(routerAgent.classify(state.getMessage()));
            return state;
        });

        graph.addNode("order", state -> {
            state.setResponse(orderAgent.chat(state.getSessionId(), state.getMessage()));
            return state;
        });

        graph.addNode("shopping", state -> {
            state.setResponse(shoppingAgent.chat(state.getSessionId(), state.getMessage()));
            return state;
        });

        graph.addNode("saveLog", state -> {
            log.info("💾 [ChatWorkflowService] 发布异步日志事件 - Session: {}", state.getSessionId());
            eventPublisher.publishEvent(new ChatLogEvent(this, state.getSessionId(), state.getMessage(), state.getResponse()));
            return state;
        });

        // 2. Define Wiring & Edges
        graph.setEntryPoint("router");

        graph.addConditionalEdge("router", state -> {
            if (state.getIntent() == IntentType.ORDER) {
                return "order";
            }
            return "shopping"; // default path for SHOPPING, GENERAL, UNKNOWN
        });

        graph.addEdge("order", "saveLog");
        graph.addEdge("shopping", "saveLog");
        graph.addEdge("saveLog", StateGraph.END);

        this.chatGraph = graph.compile();
        log.info("✅ [ChatWorkflowService] StateGraph 工作流初始化完成，节点已编排就绪");
    }

    public AgentState runWorkflow(String sessionId, String message) {
        log.info("🚀 [ChatWorkflowService] 开始执行工作流 - Session: {}, Message: {}", sessionId, message);
        AgentState initialState = new AgentState(sessionId, message);
        AgentState result = chatGraph.invoke(initialState);
        log.info("🏁 [ChatWorkflowService] 工作流执行结束 - Session: {}, Intent: {}", sessionId, result.getIntent());
        return result;
    }

    /**
     * 流式工作流：先同步走 Router 分析意图，再根据意图流式调用对应 Agent，
     * 流结束后异步保存日志。
     */
    public Flux<String> runStreamWorkflow(String sessionId, String message) {
        log.info("🚀 [ChatWorkflowService] 开始执行流式工作流 - Session: {}, Message: {}", sessionId, message);

        // Step 1: 同步路由
        IntentType intent;
        try {
            intent = routerAgent.classify(message);
        } catch (Exception e) {
            log.error("💥 [ChatWorkflowService] 意图分析失败: {}", e.getMessage());
            return Flux.just("非常抱歉，对话链路发生异常，请稍后再试。");
        }
        log.info("🔀 [ChatWorkflowService] 流式工作流路由完成 -> Intent: {}", intent);

        // Step 2: 根据意图选择流式 Agent
        Flux<String> agentStream;
        if (intent == IntentType.ORDER) {
            agentStream = orderAgent.streamChat(sessionId, message);
        } else {
            agentStream = shoppingAgent.streamChat(sessionId, message);
        }

        // Step 3: 收集完整响应，流结束后异步保存日志
        StringBuilder fullResponse = new StringBuilder();
        return agentStream
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    log.info("🏁 [ChatWorkflowService] 流式工作流结束 - Session: {}, Intent: {}", sessionId, intent);
                    eventPublisher.publishEvent(new ChatLogEvent(this, sessionId, message, fullResponse.toString()));
                })
                .doOnError(e -> log.error("💥 [ChatWorkflowService] 流式工作流异常: {}", e.getMessage()));
    }
}
