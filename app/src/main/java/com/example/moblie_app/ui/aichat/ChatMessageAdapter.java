package com.example.moblie_app.ui.aichat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
        private final LinearLayout rowBubble;
        private final LinearLayout layoutAvatar;
        private final TextView tvAvatarLabel;
        private final TextView messageText;
        private final ProgressBar progressLoading;
        private final FrameLayout bubbleContainer;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            rowBubble      = itemView.findViewById(R.id.row_bubble);
            layoutAvatar   = itemView.findViewById(R.id.layout_avatar);
            tvAvatarLabel  = itemView.findViewById(R.id.tv_avatar_label);
            messageText    = itemView.findViewById(R.id.tv_message);
            progressLoading = itemView.findViewById(R.id.progress_loading);
            bubbleContainer = itemView.findViewById(R.id.bubble_container);
        }

        void bind(ChatMessageModel message) {
            boolean fromUser = message.isFromUser();

            rowBubble.setGravity(fromUser ? Gravity.END : Gravity.START);

            if (fromUser) {
                layoutAvatar.setVisibility(View.GONE);
                bubbleContainer.setForegroundGravity(Gravity.END);
            } else {
                layoutAvatar.setVisibility(View.VISIBLE);
                tvAvatarLabel.setText("AI");
                bubbleContainer.setForegroundGravity(Gravity.START);
            }

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
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.health_chat_text_error);
            } else if (fromUser) {
                bg = R.drawable.bg_chat_bubble_user;
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
            } else {
                bg = R.drawable.bg_chat_bubble_ai;
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.health_chat_text_ai);
            }
            messageText.setBackgroundResource(bg);
            messageText.setTextColor(textColor);
        }
    }
}
