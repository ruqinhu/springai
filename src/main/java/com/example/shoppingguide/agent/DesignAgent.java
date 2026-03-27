package com.example.shoppingguide.agent;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DesignAgent {
    private static final Logger log = LoggerFactory.getLogger(DesignAgent.class);

    private final ImageModel imageModel;

    public DesignAgent(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    public String generateDesign(String description) {
        var options = ImageOptionsBuilder.builder()
                .N(1)
                .height(1024)
                .width(1024)
                .build();

        var imgPrompt = new ImagePrompt("A high quality product visualization of: " + description, options);
        log.info("🎨 [DesignAgent] 正在请求大模型生成商品设计图像...");
        var response = imageModel.call(imgPrompt);
        String url = response.getResult().getOutput().getUrl();
        log.info("🎨 [DesignAgent] 图像生成完成 URL: {}", url);

        return url;
    }
}
