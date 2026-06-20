package com.example.moblie_app.network.groq;

import com.google.gson.annotations.SerializedName;

/**
 * GroqMessage - đại diện 1 message trong mảng "messages" gửi lên
 * GroqCloud API (định dạng tương thích OpenAI chat completions).
 */
public class GroqMessage {

    @SerializedName("role")
    private String role;

    @SerializedName("content")
    private String content;

    public GroqMessage() {}

    public GroqMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole()    { return role; }
    public String getContent() { return content; }
}
