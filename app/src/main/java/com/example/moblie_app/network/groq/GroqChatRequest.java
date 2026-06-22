package com.example.moblie_app.network.groq;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * GroqChatRequest - body của POST https://api.groq.com/openai/v1/chat/completions
 */
public class GroqChatRequest {

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<GroqMessage> messages;

    @SerializedName("temperature")
    private double temperature;

    @SerializedName("max_tokens")
    private int maxTokens;

    @SerializedName("stream")
    private boolean stream;

    public GroqChatRequest(String model, List<GroqMessage> messages,
                            double temperature, int maxTokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.stream = false; // không dùng streaming để đơn giản hóa cho đồ án
    }
}
