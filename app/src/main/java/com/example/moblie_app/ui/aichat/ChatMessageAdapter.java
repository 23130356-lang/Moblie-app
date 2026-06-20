package com.example.moblie_app.ui.aichat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;
import com.example.moblie_app.model.ChatMessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatMessageAdapter - hiển thị danh sách bong bóng chat.
 * Bong bóng của user căn phải (màu xanh), của AI căn trái (màu nhạt),
 * bong bóng lỗi có viền đỏ.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private final List<ChatMessageModel> items = new ArrayList<>();

    public void submitList(List<ChatMessageModel> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView roleLabel;
        private final LinearLayout rowBubble;
        private final TextView messageText;
        private final ProgressBar progressLoading;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            roleLabel       = itemView.findViewById(R.id.tv_role_label);
            rowBubble       = itemView.findViewById(R.id.row_bubble);
            messageText     = itemView.findViewById(R.id.tv_message);
            progressLoading = itemView.findViewById(R.id.progress_loading);
        }

        void bind(ChatMessageModel message) {
            boolean fromUser = message.isFromUser();

            // Căn lề: user bên phải, AI bên trái
            rowBubble.setGravity(fromUser ? Gravity.END : Gravity.START);
            roleLabel.setText(fromUser ? "Bạn" : "AI tư vấn sức khỏe");

            if (message.isLoading()) {
                messageText.setVisibility(View.INVISIBLE);
                progressLoading.setVisibility(View.VISIBLE);
                messageText.setBackgroundResource(R.drawable.bg_chat_bubble_ai);
                messageText.setText("");
                return;
            }

            progressLoading.setVisibility(View.GONE);
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(message.getContent());

            int bg;
            int textColor;
            if (message.isError()) {
                bg = R.drawable.bg_chat_bubble_error;
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.health_error);
            } else if (fromUser) {
                bg = R.drawable.bg_chat_bubble_user;
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
            } else {
                bg = R.drawable.bg_chat_bubble_ai;
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.health_text_primary);
            }
            messageText.setBackgroundResource(bg);
            messageText.setTextColor(textColor);
        }
    }
}
