package com.example.shoppingguide.agent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RouterAgentTest {

    @Autowired
    private RouterAgent routerAgent;

    @Test
    void classifyShoppingIntent() {
        String intent = routerAgent.classify("我想买一个降噪耳机");
        assertEquals("SHOPPING", intent, "Should classify as SHOPPING");
    }

    @Test
    void classifyOrderIntent() {
        String intent = routerAgent.classify("帮我查一下订单，单号ORD123，我是Alice");
        assertEquals("ORDER", intent, "Should classify as ORDER");
    }

    @Test
    void classifyGeneralIntent() {
        String intent = routerAgent.classify("你好，很高兴认识你");
        assertEquals("GENERAL", intent, "Should classify as GENERAL");
    }
}
