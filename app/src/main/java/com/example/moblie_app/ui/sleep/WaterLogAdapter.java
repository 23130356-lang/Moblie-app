package com.example.moblie_app.ui.sleep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;
import com.example.moblie_app.model.WaterLogModel;
import com.example.moblie_app.utils.DateUtils;

import java.util.Locale;

/**
 * RecyclerView adapter cho danh sách lần uống nước trong ngày.
 */
public class WaterLogAdapter extends ListAdapter<WaterLogModel, WaterLogAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(WaterLogModel log);
    }

    private final OnDeleteClickListener deleteListener;

    public WaterLogAdapter(OnDeleteClickListener deleteListener) {
        super(DIFF_CALLBACK);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_water_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), deleteListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView  tvAmount;
        private final TextView  tvTime;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount  = itemView.findViewById(R.id.tv_water_amount);
            tvTime    = itemView.findViewById(R.id.tv_water_time);
            btnDelete = itemView.findViewById(R.id.btn_water_delete);
        }

        void bind(WaterLogModel log, OnDeleteClickListener listener) {
            tvAmount.setText(String.format(Locale.getDefault(), "%d ml", log.getAmountMl()));
            tvTime.setText(DateUtils.formatDateTime(log.getTimestamp()));
            btnDelete.setOnClickListener(v -> listener.onDelete(log));
        }
    }

    private static final DiffUtil.ItemCallback<WaterLogModel> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<WaterLogModel>() {
                @Override
                public boolean areItemsTheSame(@NonNull WaterLogModel a, @NonNull WaterLogModel b) {
                    return a.getId() != null && a.getId().equals(b.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull WaterLogModel a, @NonNull WaterLogModel b) {
                    return a.getTimestamp() == b.getTimestamp()
                            && a.getAmountMl() == b.getAmountMl();
                }
            };
}
