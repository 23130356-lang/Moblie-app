package com.example.moblie_app.ui.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;
import com.example.moblie_app.model.MealEntryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealAdapter
        extends RecyclerView.Adapter<MealViewHolder> {

    private List<MealEntryModel> mealList = new ArrayList<>();

    public interface OnMealAction {
        void onDelete(MealEntryModel meal);
    }

    private final OnMealAction listener;

    public MealAdapter(OnMealAction listener) {
        this.listener = listener;
    }

    public void setData(List<MealEntryModel> meals) {
        this.mealList = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_meal,
                        parent,
                        false);

        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MealViewHolder holder,
            int position) {

        MealEntryModel meal = mealList.get(position);

        holder.txtFoodName.setText(
                meal.getFoodName());

        holder.txtCalories.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0fg • %.0f kcal",
                        meal.getQuantity(),
                        meal.getCalories()
                ));

        holder.txtMacro.setText(
                String.format(
                        Locale.getDefault(),
                        "P %.1f | C %.1f | F %.1f",
                        meal.getProtein(),
                        meal.getCarbs(),
                        meal.getFat()
                ));

        holder.btnDelete.setOnClickListener(v -> {

            if(listener != null){
                listener.onDelete(meal);
            }

        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }
}