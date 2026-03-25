package com.example.shoppingguide.agent;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.stereotype.Service;

@Service
public class DesignAgent {

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
        var response = imageModel.call(imgPrompt);

        return response.getResult().getOutput().getUrl();
    }
}
