package com.example.shoppingguide.agent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.shoppingguide.domain.IntentType;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RouterAgentTest {

    @Autowired
    private RouterAgent routerAgent;

    @Test
    void classifyShoppingIntent() {
        IntentType intent = routerAgent.classify("我想买一个降噪耳机");
        assertEquals(IntentType.SHOPPING, intent, "Should classify as SHOPPING");
    }

    @Test
    void classifyOrderIntent() {
        IntentType intent = routerAgent.classify("帮我查一下订单，单号ORD123，我是Alice");
        assertEquals(IntentType.ORDER, intent, "Should classify as ORDER");
    }

    @Test
    void classifyGeneralIntent() {
        IntentType intent = routerAgent.classify("你好，很高兴认识你");
        assertEquals(IntentType.GENERAL, intent, "Should classify as GENERAL");
    }
}
