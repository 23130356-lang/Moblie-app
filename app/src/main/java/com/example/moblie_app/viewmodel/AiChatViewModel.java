package com.example.moblie_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.moblie_app.model.ChatMessageModel;
import com.example.moblie_app.repository.AiChatRepository;

import java.util.ArrayList;
import java.util.List;

public class AiChatViewModel extends AndroidViewModel {

    private final AiChatRepository repository;
    private final MutableLiveData<List<ChatMessageModel>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String>  errorEvent = new MutableLiveData<>();
    private final List<ChatMessageModel> conversationHistory = new ArrayList<>();

    public AiChatViewModel(@NonNull Application application) {
        super(application);
        repository = new AiChatRepository(application);
    }

    public MutableLiveData<List<ChatMessageModel>> getMessages()  { return messages; }
    public MutableLiveData<Boolean>                getIsLoading() { return isLoading; }
    public MutableLiveData<String>                 getErrorEvent(){ return errorEvent; }

    public void sendUserMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;

        ChatMessageModel userMsg = new ChatMessageModel(ChatMessageModel.ROLE_USER, text.trim());
        conversationHistory.add(userMsg);

        List<ChatMessageModel> current = new ArrayList<>(messages.getValue());
        current.add(userMsg);

        ChatMessageModel loadingMsg = ChatMessageModel.loadingMessage();
        current.add(loadingMsg);
        messages.setValue(current);

        isLoading.setValue(true);

        MutableLiveData<String> replyLiveData = new MutableLiveData<>();
        MutableLiveData<String> errorLiveData = new MutableLiveData<>();

        final androidx.lifecycle.Observer<String>[] replyObserverHolder = new androidx.lifecycle.Observer[1];
        final androidx.lifecycle.Observer<String>[] errorObserverHolder = new androidx.lifecycle.Observer[1];

        replyObserverHolder[0] = reply -> {
            if (reply == null) return;
            replaceLoadingBubble(new ChatMessageModel(ChatMessageModel.ROLE_ASSISTANT, reply));
            conversationHistory.add(new ChatMessageModel(ChatMessageModel.ROLE_ASSISTANT, reply));
            replyLiveData.removeObserver(replyObserverHolder[0]);
            errorLiveData.removeObserver(errorObserverHolder[0]);
        };

        errorObserverHolder[0] = err -> {
            if (err == null) return;
            ChatMessageModel errorMsg = new ChatMessageModel(ChatMessageModel.ROLE_ASSISTANT, err);
            errorMsg.setError(true);
            replaceLoadingBubble(errorMsg);
            errorEvent.setValue(err);
            replyLiveData.removeObserver(replyObserverHolder[0]);
            errorLiveData.removeObserver(errorObserverHolder[0]);
        };

        replyLiveData.observeForever(replyObserverHolder[0]);
        errorLiveData.observeForever(errorObserverHolder[0]);

        List<ChatMessageModel> messagesToSendToApi = new ArrayList<>();
        messagesToSendToApi.add(new ChatMessageModel(ChatMessageModel.ROLE_SYSTEM,
                "Bạn là trợ lý AI tư vấn sức khỏe. Chỉ trả lời ngắn gọn các câu hỏi liên quan đến sức khỏe, dinh dưỡng, vận động. Tuyệt đối từ chối và xin lỗi lịch sự nếu người dùng hỏi về lập trình, mã code (HTML, CSS, JS...), hoặc các lĩnh vực khác không liên quan để tiết kiệm token tối đa."));

        messagesToSendToApi.addAll(conversationHistory);

        repository.sendMessage(messagesToSendToApi, replyLiveData, errorLiveData, isLoading);
    }

    private void replaceLoadingBubble(ChatMessageModel newMessage) {
        List<ChatMessageModel> current = new ArrayList<>(messages.getValue());
        for (int i = current.size() - 1; i >= 0; i--) {
            if (current.get(i).isLoading()) {
                current.set(i, newMessage);
                break;
            }
        }
        messages.setValue(current);
    }

    public void clearConversation() {
        conversationHistory.clear();
        messages.setValue(new ArrayList<>());
    }
}