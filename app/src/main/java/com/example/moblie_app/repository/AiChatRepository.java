package com.example.moblie_app.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.ChatMessageModel;
import com.example.moblie_app.model.HealthGoalsModel;
import com.example.moblie_app.network.groq.GroqApiClient;
import com.example.moblie_app.network.groq.GroqChatRequest;
import com.example.moblie_app.network.groq.GroqChatResponse;
import com.example.moblie_app.network.groq.GroqMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AiChatRepository
 * - Gửi hội thoại tới GroqCloud API (console.groq.com) và trả lời tư vấn sức khỏe.
 * - Tự động chèn "system prompt" định hướng vai trò trợ lý sức khỏe,
 *   kèm theo context mục tiêu sức khỏe hiện tại của người dùng (nếu có)
 *   để model đưa ra lời khuyên cá nhân hóa hơn.
 *
 * Theo đúng pattern callback + MutableLiveData như các Repository khác
 * trong đồ án (xem HealthGoalsRepository).
 */
public class AiChatRepository extends BaseRepository {

    // Model GroqCloud hiện tại được khuyến nghị (nhanh, miễn phí dùng thử,
    // thay thế cho llama-3.1-8b-instant / llama-3.3-70b-versatile đã deprecated).
    // Đổi sang "openai/gpt-oss-120b" nếu cần câu trả lời chất lượng cao hơn (chậm hơn một chút).
    private static final String MODEL_NAME   = "openai/gpt-oss-20b";
    private static final double TEMPERATURE  = 0.7;
    private static final int    MAX_TOKENS   = 800;

    private static final String SYSTEM_PROMPT =
            "Bạn là trợ lý AI tư vấn sức khỏe trong một ứng dụng theo dõi sức khỏe di động. " +
            "Nhiệm vụ của bạn:\n" +
            "- Trả lời bằng tiếng Việt, ngắn gọn, dễ hiểu, thân thiện.\n" +
            "- Đưa ra lời khuyên về dinh dưỡng, vận động, giấc ngủ, lối sống lành mạnh " +
            "dựa trên thông tin người dùng cung cấp.\n" +
            "- Nếu có dữ liệu mục tiêu sức khỏe của người dùng (cân nặng mục tiêu, " +
            "lượng calo, số bước chân mỗi ngày), hãy tham khảo để đưa lời khuyên phù hợp.\n" +
            "- KHÔNG chẩn đoán bệnh, KHÔNG kê đơn thuốc, KHÔNG thay thế bác sĩ. " +
            "Nếu người dùng mô tả triệu chứng nghiêm trọng hoặc cấp cứu, khuyên họ " +
            "đến cơ sở y tế gần nhất hoặc gọi cấp cứu ngay.\n" +
            "- Giữ câu trả lời súc tích (khoảng 3-6 câu), trừ khi người dùng yêu cầu chi tiết hơn.";

    private final Context appContext;
    private final HealthGoalsRepository healthGoalsRepository;

    public AiChatRepository(Context context) {
        super();
        this.appContext = context.getApplicationContext();
        this.healthGoalsRepository = new HealthGoalsRepository(appContext);
    }

    /**
     * Gửi toàn bộ lịch sử hội thoại (chưa bao gồm system prompt) lên GroqCloud
     * và trả lời qua các LiveData callback.
     *
     * @param conversationHistory danh sách tin nhắn user/assistant theo thứ tự thời gian
     * @param replyLiveData       nhận nội dung trả lời khi thành công
     * @param errorLiveData       nhận thông báo lỗi khi thất bại
     * @param isLoading           bật/tắt trạng thái đang gửi
     */
    public void sendMessage(List<ChatMessageModel> conversationHistory,
                             MutableLiveData<String> replyLiveData,
                             MutableLiveData<String> errorLiveData,
                             MutableLiveData<Boolean> isLoading) {

        if (!GroqApiClient.isApiKeyConfigured()) {
            isLoading.setValue(false);
            errorLiveData.setValue(
                    "Chưa cấu hình Groq API key. Vui lòng thêm GROQ_API_KEY vào " +
                    "file local.properties rồi build lại ứng dụng.");
            return;
        }

        // Lấy context mục tiêu sức khỏe hiện có (đọc từ cache local, không cần đợi mạng)
        HealthGoalsModel goals = healthGoalsRepository.loadFromPrefs();

        List<GroqMessage> messages = new ArrayList<>();
        messages.add(new GroqMessage(ChatMessageModel.ROLE_SYSTEM, buildSystemPrompt(goals)));

        for (ChatMessageModel m : conversationHistory) {
            // Không gửi các tin nhắn đang loading/lỗi lên API
            if (m.isLoading()) continue;
            messages.add(new GroqMessage(m.getRole(), m.getContent()));
        }

        GroqChatRequest request = new GroqChatRequest(MODEL_NAME, messages, TEMPERATURE, MAX_TOKENS);

        GroqApiClient.getService().sendChat(request).enqueue(new Callback<GroqChatResponse>() {
            @Override
            public void onResponse(Call<GroqChatResponse> call, Response<GroqChatResponse> response) {
                isLoading.setValue(false);

                if (!response.isSuccessful() || response.body() == null) {
                    errorLiveData.setValue(parseHttpError(response));
                    return;
                }

                GroqChatResponse body = response.body();
                if (body.getError() != null) {
                    errorLiveData.setValue("Lỗi từ Groq: " + body.getError().getMessage());
                    return;
                }

                String content = body.getFirstMessageContent();
                if (content == null || content.trim().isEmpty()) {
                    errorLiveData.setValue("Groq không trả về nội dung. Vui lòng thử lại.");
                    return;
                }

                replyLiveData.setValue(content.trim());
            }

            @Override
            public void onFailure(Call<GroqChatResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorLiveData.setValue("Không thể kết nối tới Groq: " + t.getMessage());
            }
        });
    }

    /**
     * Ghép system prompt cố định với context động (mục tiêu sức khỏe người dùng),
     * giúp model tư vấn cá nhân hóa hơn thay vì chung chung.
     */
    private String buildSystemPrompt(HealthGoalsModel goals) {
        if (goals == null) return SYSTEM_PROMPT;

        String context = String.format(Locale.US,
                "\n\nThông tin mục tiêu sức khỏe hiện tại của người dùng (nếu phù hợp, hãy tham khảo " +
                "khi tư vấn): cân nặng mục tiêu %.1f kg, lượng calo mục tiêu %d kcal/ngày, " +
                "số bước chân mục tiêu %d bước/ngày.",
                goals.getTargetWeight(), goals.getTargetCalories(), goals.getTargetSteps());

        return SYSTEM_PROMPT + context;
    }

    private String parseHttpError(Response<GroqChatResponse> response) {
        int code = response.code();
        if (code == 401) {
            return "API key không hợp lệ hoặc đã hết hạn (401). Kiểm tra lại GROQ_API_KEY.";
        } else if (code == 429) {
            return "Đã vượt giới hạn yêu cầu tới Groq (429). Vui lòng thử lại sau ít phút.";
        } else if (code >= 500) {
            return "Máy chủ Groq đang gặp sự cố (" + code + "). Vui lòng thử lại sau.";
        }
        return "Lỗi gọi Groq API (mã " + code + "). Vui lòng thử lại.";
    }
}
