package com.example.shoppingguide.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A lightweight State Graph engine for orchestrating Agent workflows.
 */
public class StateGraph {
    private static final Logger log = LoggerFactory.getLogger(StateGraph.class);
    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, String> edges = new HashMap<>();
    private final Map<String, Function<AgentState, String>> conditionalEdges = new HashMap<>();
    private String entryPoint;

    public static final String END = "__END__";

    public StateGraph addNode(String name, Node node) {
        nodes.put(name, node);
        return this;
    }

    public StateGraph addEdge(String from, String to) {
        edges.put(from, to);
        return this;
    }

    public StateGraph addConditionalEdge(String from, Function<AgentState, String> condition) {
        conditionalEdges.put(from, condition);
        return this;
    }

    public StateGraph setEntryPoint(String nodeName) {
        this.entryPoint = nodeName;
        return this;
    }

    public CompiledGraph compile() {
        if (entryPoint == null || !nodes.containsKey(entryPoint)) {
            throw new IllegalStateException("Valid entry point must be set before compiling.");
        }
        return new CompiledGraph(nodes, edges, conditionalEdges, entryPoint);
    }

    public static class CompiledGraph {
        private final Map<String, Node> nodes;
        private final Map<String, String> edges;
        private final Map<String, Function<AgentState, String>> conditionalEdges;
        private final String entryPoint;

        public CompiledGraph(Map<String, Node> nodes, Map<String, String> edges, 
                             Map<String, Function<AgentState, String>> conditionalEdges, String entryPoint) {
            this.nodes = new HashMap<>(nodes);
            this.edges = new HashMap<>(edges);
            this.conditionalEdges = new HashMap<>(conditionalEdges);
            this.entryPoint = entryPoint;
        }

        public AgentState invoke(AgentState state) {
            String currentNode = entryPoint;
            log.info("🔥 [Workflow Start] Session: {}", state.getSessionId());

            while (!END.equals(currentNode) && currentNode != null) {
                Node node = nodes.get(currentNode);
                if (node == null) {
                    state.setResponse("非常抱歉，对话链路发生异常，未找到节点：" + currentNode);
                    log.error("❌ [Workflow Error] Node not found: {}", currentNode);
                    break;
                }

                try {
                    // Process the current node
                    log.info("⏳ [Executing Node] --> {}", currentNode);
                    state = node.process(state);
                    log.info("✅ [Node Completed] <-- {}", currentNode);

                    // Determine next node
                    if (conditionalEdges.containsKey(currentNode)) {
                        String nextNode = conditionalEdges.get(currentNode).apply(state);
                        log.info("🔀 [Conditional Edge] {} evaluates to --> {}", currentNode, nextNode);
                        currentNode = nextNode;
                    } else {
                        currentNode = edges.get(currentNode);
                        log.info("➡️ [Direct Edge] Moving to --> {}", currentNode);
                    }
                } catch (Exception e) {
                    // 回退逻辑 (Fallback)
                    state.setResponse("非常抱歉，网络服务繁忙或发生异常，请稍后再试。(" + e.getMessage() + ")");
                    log.error("💥 [Workflow Failed] Exception at node {}: {}", currentNode, e.getMessage(), e);
                    break;
                }
            }

            log.info("🏁 [Workflow End] Session: {}, Final Intent: {}", state.getSessionId(), state.getIntent());
            return state;
        }
    }
}
