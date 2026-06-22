package com.example.moblie_app.model;

/**
 * ChatMessageModel - POJO đại diện cho một tin nhắn trong cuộc hội thoại
 * với AI tư vấn sức khỏe (GroqCloud).
 *
 * role: "user" | "assistant" | "system" (theo chuẩn role tương thích OpenAI API)
 */
public class ChatMessageModel {

    public static final String ROLE_USER      = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM    = "system";

    private String role;
    private String content;
    private long   timestamp;

    // Đánh dấu tin nhắn đang chờ phản hồi (hiện "..." loading bubble)
    private boolean isLoading;

    // Đánh dấu tin nhắn lỗi (hiện màu đỏ + cho phép gửi lại)
    private boolean isError;

    public ChatMessageModel() {
        // Constructor rỗng
    }

    public ChatMessageModel(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public static ChatMessageModel loadingMessage() {
        ChatMessageModel m = new ChatMessageModel(ROLE_ASSISTANT, "");
        m.isLoading = true;
        return m;
    }

    public String getRole()                  { return role; }
    public void   setRole(String role)        { this.role = role; }

    public String getContent()                { return content; }
    public void   setContent(String content)  { this.content = content; }

    public long   getTimestamp()              { return timestamp; }
    public void   setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isLoading()                { return isLoading; }
    public void    setLoading(boolean loading) { this.isLoading = loading; }

    public boolean isError()                  { return isError; }
    public void    setError(boolean error)     { this.isError = error; }

    public boolean isFromUser() {
        return ROLE_USER.equals(role);
    }
}
