package com.example.moblie_app.ui.diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moblie_app.R;
import com.example.moblie_app.databinding.FragmentFoodDiaryBinding;
import com.example.moblie_app.model.DailyNutritionSummary;
import com.example.moblie_app.model.FavoriteFoodModel;
import com.example.moblie_app.model.MealEntryModel;
import com.example.moblie_app.model.MealSummary;
import com.example.moblie_app.model.MacroNutrients;
import com.example.moblie_app.utils.DateUtils;
import com.example.moblie_app.viewmodel.FavoriteFoodViewModel;
import com.example.moblie_app.viewmodel.NutritionViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FoodDiaryFragment extends Fragment {

    private FragmentFoodDiaryBinding binding;
    private FoodDiaryAdapter adapter;
    private NutritionViewModel viewModel;
    private FavoriteFoodViewModel favViewModel;
    private String currentDate;
    private List<String> favNames = new ArrayList<>();

    private static final double DAILY_KCAL_GOAL = 2000;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFoodDiaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NutritionViewModel.class);
        favViewModel = new ViewModelProvider(
                this,
                new FavoriteFoodViewModel.Factory(requireContext()))
                .get(FavoriteFoodViewModel.class);

        currentDate = DateUtils.getTodayKey();
        binding.tvSelectedDate.setText(DateUtils.getTodayDisplay());

        adapter = new FoodDiaryAdapter(
                this::deleteMeal,
                this::showEditQuantityDialog,
                this::onFavToggle);
        binding.rvFoodDiary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFoodDiary.setAdapter(adapter);

        observeViewModel();

        favViewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            favNames = new ArrayList<>();
            if (favorites != null) {
                for (FavoriteFoodModel m : favorites) {
                    if (m.getName() != null) favNames.add(m.getName().toLowerCase());
                }
            }
            adapter.setFavoriteNames(favNames);
        });

        favViewModel.loadFavorites();
        viewModel.loadEntries(currentDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        favViewModel.loadFavorites();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                binding.tvError.setText(msg);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });

        viewModel.getEntries().observe(getViewLifecycleOwner(), meals -> {
            if (meals != null) {
                adapter.submitList(meals);
                boolean empty = meals.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.rvFoodDiary.setVisibility(empty ? View.GONE : View.VISIBLE);
                binding.tvTotalItems.setText(String.format(Locale.getDefault(), "%d món", meals.size()));
            }
        });

        viewModel.getDailySummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                bindSummary(summary);
            }
        });
    }

    private void bindSummary(DailyNutritionSummary summary) {
        MacroNutrients dailyTotal = summary.getDailyTotal();
        if (dailyTotal != null) {
            double kcal = dailyTotal.getCalories();
            binding.tvTotalCalories.setText(String.format(Locale.getDefault(), "%.0f", kcal));
            binding.tvDailyProtein.setText(String.format(Locale.getDefault(), "%.1f", dailyTotal.getProtein()));
            binding.tvDailyCarbs.setText(String.format(Locale.getDefault(), "%.1f", dailyTotal.getCarbs()));
            binding.tvDailyFat.setText(String.format(Locale.getDefault(), "%.1f", dailyTotal.getFat()));

            int progress = (int) Math.min((kcal / DAILY_KCAL_GOAL) * 100, 100);
            binding.progressKcal.setProgress(progress);
        }

        bindMealSummary(summary.getBreakfast(), binding.tvBreakfastCalories);
        bindMealSummary(summary.getLunch(), binding.tvLunchCalories);
        bindMealSummary(summary.getDinner(), binding.tvDinnerCalories);
        bindMealSummary(summary.getSnack(), binding.tvSnackCalories);
    }

    private void bindMealSummary(MealSummary mealSummary, android.widget.TextView calTextView) {
        if (mealSummary != null && calTextView != null) {
            MacroNutrients n = mealSummary.getNutrients();
            calTextView.setText(String.format(Locale.getDefault(),
                    "%.0f kcal", n != null ? n.getCalories() : 0));
        }
    }

    private void showEditQuantityDialog(MealEntryModel meal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa khẩu phần");
        builder.setMessage("Nhập số gram mới cho " + meal.getFoodName());

        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf((int) meal.getQuantity()));
        input.setSelectAllOnFocus(true);

        int paddingPx = (int) (getResources().getDisplayMetrics().density * 24);
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setPadding(paddingPx, 0, paddingPx, 0);
        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String qtyStr = input.getText().toString().trim();
            if (qtyStr.isEmpty()) return;

            try {
                double newQuantity = Double.parseDouble(qtyStr);
                if (newQuantity <= 0) {
                    binding.tvError.setText("Khẩu phần phải lớn hơn 0 gram.");
                    binding.tvError.setVisibility(View.VISIBLE);
                    return;
                }
                updateMealQuantity(meal, newQuantity);
            } catch (NumberFormatException e) {
                binding.tvError.setText("Số gram không hợp lệ.");
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateMealQuantity(MealEntryModel meal, double newQuantity) {
        double oldQuantity = meal.getQuantity();
        if (oldQuantity <= 0) return;

        double calsPer100g = meal.getCalories() * 100.0 / oldQuantity;
        double protPer100g = meal.getProtein() * 100.0 / oldQuantity;
        double carbsPer100g = meal.getCarbs() * 100.0 / oldQuantity;
        double fatPer100g = meal.getFat() * 100.0 / oldQuantity;

        MacroNutrients recalculated = NutritionViewModel.calcNutrition(
                calsPer100g, protPer100g, carbsPer100g, fatPer100g, newQuantity
        );

        meal.setQuantity(newQuantity);
        meal.setCalories(recalculated.getCalories());
        meal.setProtein(recalculated.getProtein());
        meal.setCarbs(recalculated.getCarbs());
        meal.setFat(recalculated.getFat());

        viewModel.updateEntry(meal);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String[] parts = currentDate.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);

        DatePickerDialog picker = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    currentDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                    binding.tvSelectedDate.setText(String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", d, m + 1, y));
                    viewModel.loadEntries(currentDate);
                },
                year, month, day);
        picker.show();
    }

    private void deleteMeal(MealEntryModel meal) {
        if (meal.getId() == null) return;
        viewModel.deleteEntry(currentDate, meal.getId());
    }

    private void onFavToggle(MealEntryModel meal) {
        String name = meal.getFoodName();
        if (name == null) return;
        if (favNames.contains(name.toLowerCase())) {
            favViewModel.removeFavorite(name);
        } else {
            favViewModel.addFavorite(name, meal.getCalories(), meal.getProtein(),
                    meal.getCarbs(), meal.getFat(), "Nhật ký ăn uống");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
