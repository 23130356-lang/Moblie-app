package com.example.moblie_app.ui.sleep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;
import com.example.moblie_app.model.SleepLogModel;
import com.example.moblie_app.utils.SleepAnalyzer;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * RecyclerView adapter cho danh sách nhật ký giấc ngủ.
 */
public class SleepLogAdapter extends ListAdapter<SleepLogModel, SleepLogAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(SleepLogModel log);
    }

    private final OnDeleteClickListener deleteListener;

    public SleepLogAdapter(OnDeleteClickListener deleteListener) {
        super(DIFF_CALLBACK);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), deleteListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView       tvDate;
        private final TextView       tvTime;
        private final TextView       tvDuration;
        private final TextView       tvQuality;
        private final TextView       tvNote;
        private final MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate     = itemView.findViewById(R.id.tv_sleep_date);
            tvTime     = itemView.findViewById(R.id.tv_sleep_time);
            tvDuration = itemView.findViewById(R.id.tv_sleep_duration);
            tvQuality  = itemView.findViewById(R.id.tv_sleep_quality);
            tvNote     = itemView.findViewById(R.id.tv_sleep_note);
            btnDelete  = itemView.findViewById(R.id.btn_sleep_delete);
        }

        void bind(SleepLogModel log, OnDeleteClickListener listener) {
            tvDate.setText(log.getDateKey());
            tvTime.setText(String.format(Locale.getDefault(),
                    "%s → %s", log.getBedTime(), log.getWakeTime()));
            tvDuration.setText(SleepAnalyzer.formatDuration(log.getDurationHours()));
            tvQuality.setText(SleepAnalyzer.qualityLabel(log.getQuality()));

            if (log.getNote() != null && !log.getNote().trim().isEmpty()) {
                tvNote.setText(log.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            btnDelete.setOnClickListener(v -> listener.onDelete(log));
        }
    }

    private static final DiffUtil.ItemCallback<SleepLogModel> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SleepLogModel>() {
                @Override
                public boolean areItemsTheSame(@NonNull SleepLogModel a, @NonNull SleepLogModel b) {
                    return a.getId() != null && a.getId().equals(b.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull SleepLogModel a, @NonNull SleepLogModel b) {
                    return a.getTimestamp() == b.getTimestamp()
                            && a.getDurationHours() == b.getDurationHours();
                }
            };
}
