package com.example.moblie_app.network.groq;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * GroqChatResponse - parse JSON trả về từ GroqCloud chat completions API.
 *
 * Cấu trúc rút gọn, chỉ lấy field cần dùng:
 * {
 *   "choices": [
 *     { "message": { "role": "assistant", "content": "..." }, "finish_reason": "stop" }
 *   ],
 *   "usage": { ... }
 * }
 */
public class GroqChatResponse {

    @SerializedName("choices")
    private List<Choice> choices;

    @SerializedName("error")
    private GroqError error;

    public List<Choice> getChoices() { return choices; }
    public GroqError    getError()   { return error; }

    /** Lấy nội dung text trả lời đầu tiên, hoặc null nếu không có. */
    public String getFirstMessageContent() {
        if (choices == null || choices.isEmpty()) return null;
        Choice c = choices.get(0);
        if (c == null || c.message == null) return null;
        return c.message.getContent();
    }

    public static class Choice {
        @SerializedName("message")
        private GroqMessage message;

        @SerializedName("finish_reason")
        private String finishReason;
    }

    public static class GroqError {
        @SerializedName("message")
        private String message;

        @SerializedName("code")
        private String code;

        public String getMessage() { return message; }
        public String getCode()    { return code; }
    }
}
