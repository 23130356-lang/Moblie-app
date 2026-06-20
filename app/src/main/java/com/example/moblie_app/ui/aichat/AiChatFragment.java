package com.example.moblie_app.ui.aichat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;
import com.example.moblie_app.model.ChatMessageModel;
import com.example.moblie_app.viewmodel.AiChatViewModel;

import java.util.List;

/**
 * AiChatFragment - màn hình chat với AI tư vấn sức khỏe (GroqCloud API).
 * Truy cập thông qua nút FAB nổi ở mọi màn hình chính (xem MainActivity).
 */
public class AiChatFragment extends Fragment {

    private AiChatViewModel viewModel;
    private ChatMessageAdapter adapter;

    private RecyclerView recyclerMessages;
    private LinearLayout emptyState;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnBack;
    private ImageButton btnClearChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AiChatViewModel.class);

        bindViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void bindViews(View view) {
        recyclerMessages = view.findViewById(R.id.recycler_messages);
        emptyState       = view.findViewById(R.id.empty_state);
        etMessage        = view.findViewById(R.id.et_message);
        btnSend          = view.findViewById(R.id.btn_send);
        btnBack          = view.findViewById(R.id.btn_back);
        btnClearChat     = view.findViewById(R.id.btn_clear_chat);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        btnClearChat.setOnClickListener(v -> viewModel.clearConversation());

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etMessage.getText().toString();
        if (TextUtils.isEmpty(text.trim())) return;

        viewModel.sendUserMessage(text);
        etMessage.setText("");
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), this::renderMessages);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean enabled = loading == null || !loading;
            btnSend.setEnabled(enabled);
            btnSend.setAlpha(enabled ? 1f : 0.5f);
        });

        viewModel.getErrorEvent().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderMessages(List<ChatMessageModel> messages) {
        boolean isEmpty = messages == null || messages.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerMessages.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) return;

        adapter.submitList(messages);
        recyclerMessages.scrollToPosition(messages.size() - 1);
    }
}
