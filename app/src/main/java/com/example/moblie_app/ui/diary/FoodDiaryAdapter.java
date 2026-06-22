package com.example.moblie_app.ui.diary;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;
import com.example.moblie_app.databinding.ItemFoodDiaryBinding;
import com.example.moblie_app.model.MealEntryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodDiaryAdapter extends RecyclerView.Adapter<FoodDiaryAdapter.FoodViewHolder> {

    public interface OnDeleteListener {
        void onDelete(MealEntryModel meal);
    }

    public interface OnEditListener {
        void onEdit(MealEntryModel meal);
    }

    private final List<MealEntryModel> items = new ArrayList<>();
    private final OnDeleteListener deleteListener;
    private final OnEditListener editListener;

    public FoodDiaryAdapter(OnDeleteListener deleteListener,
                            OnEditListener editListener) {
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void submitList(List<MealEntryModel> meals) {
        items.clear();
        if (meals != null) {
            items.addAll(meals);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodDiaryBinding binding = ItemFoodDiaryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        holder.bind(items.get(position), deleteListener, editListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemFoodDiaryBinding binding;

        FoodViewHolder(ItemFoodDiaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MealEntryModel meal,
                  OnDeleteListener deleteListener,
                  OnEditListener editListener) {
            binding.tvFoodName.setText(meal.getFoodName());

            String mealType = meal.getMealType();
            binding.tvMealType.setText(formatMealType(mealType));

            int iconRes = getMealIcon(mealType);
            binding.ivMealIcon.setImageResource(iconRes);

            binding.tvCalories.setText(String.format(Locale.getDefault(),
                    "%.0f kcal", meal.getCalories()));
            binding.tvProtein.setText(String.format(Locale.getDefault(),
                    "P: %.1f", meal.getProtein()));
            binding.tvCarbs.setText(String.format(Locale.getDefault(),
                    "C: %.1f", meal.getCarbs()));
            binding.tvFat.setText(String.format(Locale.getDefault(),
                    "F: %.1f", meal.getFat()));
            binding.tvQuantity.setText(String.format(Locale.getDefault(),
                    "Khẩu phần: %.0f g", meal.getQuantity()));
            binding.btnEdit.setOnClickListener(v -> editListener.onEdit(meal));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(meal));
        }

        private int getMealIcon(String type) {
            if (type == null) return R.drawable.ic_snack;
            switch (type) {
                case "breakfast": return R.drawable.ic_breakfast;
                case "lunch":     return R.drawable.ic_lunch;
                case "dinner":    return R.drawable.ic_dinner;
                case "snack":     return R.drawable.ic_snack;
                default:          return R.drawable.ic_snack;
            }
        }

        private String formatMealType(String type) {
            if (type == null) return "";
            switch (type) {
                case "breakfast": return "Bữa sáng";
                case "lunch":     return "Bữa trưa";
                case "dinner":    return "Bữa tối";
                case "snack":     return "Bữa phụ";
                default:          return type;
            }
        }
    }
}
