package com.example.moblie_app.ui.diary;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;
import com.example.moblie_app.databinding.ItemFoodDiaryBinding;
import com.example.moblie_app.model.MealEntryModel;
import com.example.moblie_app.utils.ServingUnit;

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

    public interface OnFavListener {
        void onFav(MealEntryModel meal);
    }

    private final List<MealEntryModel> items = new ArrayList<>();
    private final OnDeleteListener deleteListener;
    private final OnEditListener editListener;
    private final OnFavListener favListener;
    private List<String> favoriteNames = new ArrayList<>();

    public FoodDiaryAdapter(OnDeleteListener deleteListener,
                            OnEditListener editListener,
                            OnFavListener favListener) {
        this.deleteListener = deleteListener;
        this.editListener = editListener;
        this.favListener = favListener;
    }

    public void submitList(List<MealEntryModel> meals) {
        items.clear();
        if (meals != null) {
            items.addAll(meals);
        }
        notifyDataSetChanged();
    }

    public void setFavoriteNames(List<String> names) {
        this.favoriteNames = names != null ? names : new ArrayList<>();
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
        holder.bind(items.get(position), deleteListener, editListener, favListener, favoriteNames);
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
                  OnEditListener editListener,
                  OnFavListener favListener,
                  List<String> favoriteNames) {
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
            String qtyDisplay = ServingUnit.gramsToDisplay(
                    meal.getQuantity(), meal.getUnitLabel(), meal.getUnitQuantity());
            binding.tvQuantity.setText("Khẩu phần: " + qtyDisplay);
            binding.btnEdit.setOnClickListener(v -> editListener.onEdit(meal));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(meal));

            // Trạng thái nút yêu thích
            boolean fav = meal.getFoodName() != null
                    && favoriteNames.contains(meal.getFoodName().toLowerCase());
            android.content.res.ColorStateList tint = fav
                    ? android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(
                                    binding.getRoot().getContext(), R.color.health_mint))
                    : android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(
                                    binding.getRoot().getContext(), R.color.health_text_hint));
            binding.btnFavorite.setIconResource(fav
                    ? R.drawable.ic_heart_filled
                    : R.drawable.ic_heart_outline);
            binding.btnFavorite.setIconTint(tint);
            binding.btnFavorite.setOnClickListener(v -> favListener.onFav(meal));
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
