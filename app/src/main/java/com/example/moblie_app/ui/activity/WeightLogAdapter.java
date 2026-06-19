package com.example.moblie_app.ui.activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.databinding.ItemWeightLogBinding;
import com.example.moblie_app.model.WeightLogModel;
import com.example.moblie_app.utils.BmiCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeightLogAdapter extends RecyclerView.Adapter<WeightLogAdapter.WeightViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(WeightLogModel log);
    }

    private final List<WeightLogModel> items = new ArrayList<>();
    private final OnDeleteClickListener deleteClickListener;

    public WeightLogAdapter(OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void submitList(List<WeightLogModel> logs) {
        items.clear();
        if (logs != null) {
            items.addAll(logs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeightLogBinding binding = ItemWeightLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WeightViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        holder.bind(items.get(position), deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {
        private final ItemWeightLogBinding binding;

        WeightViewHolder(ItemWeightLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WeightLogModel log, OnDeleteClickListener deleteClickListener) {
            binding.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", log.getWeightKg()));
            binding.tvBmi.setText("BMI " + BmiCalculator.format(log.getBmi())
                    + " · " + log.getBmiCategory());
            binding.tvDate.setText(log.getDateKey() + " · " + String.format(Locale.getDefault(),
                    "%.0f cm", log.getHeightCm()));
            binding.tvNote.setText(log.getNote() == null || log.getNote().trim().isEmpty()
                    ? "Không có ghi chú"
                    : log.getNote().trim());
            binding.btnDeleteWeight.setOnClickListener(v -> deleteClickListener.onDelete(log));
        }
    }
}
