package com.example.moblie_app.ui.activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.databinding.ItemActivityLogBinding;
import com.example.moblie_app.model.ActivityLogModel;
import com.example.moblie_app.utils.ActivityCalculator;
import com.example.moblie_app.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ActivityViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(ActivityLogModel log);
    }

    private final List<ActivityLogModel> items = new ArrayList<>();
    private final OnDeleteClickListener deleteClickListener;

    public ActivityLogAdapter(OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void submitList(List<ActivityLogModel> logs) {
        items.clear();
        if (logs != null) {
            items.addAll(logs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActivityLogBinding binding = ItemActivityLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ActivityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        holder.bind(items.get(position), deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final ItemActivityLogBinding binding;

        ActivityViewHolder(ItemActivityLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ActivityLogModel log, OnDeleteClickListener deleteClickListener) {
            binding.tvTitle.setText(log.getTitle());
            binding.tvDate.setText(log.getDateKey());
            binding.tvDetails.setText(buildDetails(log));
            binding.tvCalories.setText(ActivityCalculator.formatCalories(log.getCaloriesBurned()));
            binding.btnDelete.setOnClickListener(v -> deleteClickListener.onDelete(log));
        }

        private String buildDetails(ActivityLogModel log) {
            StringBuilder builder = new StringBuilder();
            if (Constants.ACTIVITY_STEPS.equals(log.getActivityType())) {
                builder.append(String.format(Locale.getDefault(), "%d bước · %s",
                        log.getStepCount(),
                        ActivityCalculator.formatDistance(log.getDistanceKm())));
            } else {
                builder.append(log.getDurationMinutes()).append(" phút");
            }

            if (log.getNote() != null && !log.getNote().trim().isEmpty()) {
                builder.append("\n").append(log.getNote().trim());
            }
            return builder.toString();
        }
    }
}
