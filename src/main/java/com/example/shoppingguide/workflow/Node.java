package com.example.shoppingguide.workflow;

@FunctionalInterface
public interface Node {
    AgentState process(AgentState state);
}
