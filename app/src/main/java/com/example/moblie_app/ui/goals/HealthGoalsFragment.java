package com.example.moblie_app.ui.goals;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.moblie_app.R;
import com.example.moblie_app.model.HealthGoalsModel;
import com.example.moblie_app.viewmodel.HealthGoalsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class HealthGoalsFragment extends Fragment {

    private HealthGoalsViewModel viewModel;

    // Views
    private Slider            sliderWeight, sliderCalories, sliderSteps;
    private TextView          tvWeightBadge, tvCaloriesBadge, tvStepsBadge;
    private TextInputLayout   tilWeight, tilCalories, tilSteps;
    private TextInputEditText etWeight, etCalories, etSteps;
    private ChipGroup         chipGroupCalories, chipGroupSteps;
    private MaterialButton    btnSave, btnBack;
    private TextView          tvMessage;
    private ProgressBar       progressBar;

    // Single master flag — khi đang update từ bất kỳ nguồn nào,
    // tất cả listener khác đều bỏ qua để tránh vòng lặp vô hạn.
    private boolean isUpdating = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        viewModel = new ViewModelProvider(this).get(HealthGoalsViewModel.class);

        setupSliderListeners();
        setupEditTextListeners();
        setupChipListeners();
        observeViewModel();

        btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        btnSave.setOnClickListener(v -> {
            String w = etWeight.getText()   != null ? etWeight.getText().toString()   : "";
            String c = etCalories.getText() != null ? etCalories.getText().toString() : "";
            String s = etSteps.getText()    != null ? etSteps.getText().toString()    : "";
            viewModel.saveGoals(w, c, s);
        });

        viewModel.loadGoals();
    }

    // ----------------------------------------------------------------
    // Bind views
    // ----------------------------------------------------------------
    private void bindViews(View v) {
        sliderWeight      = v.findViewById(R.id.slider_weight);
        sliderCalories    = v.findViewById(R.id.slider_calories);
        sliderSteps       = v.findViewById(R.id.slider_steps);
        tvWeightBadge     = v.findViewById(R.id.tv_weight_badge);
        tvCaloriesBadge   = v.findViewById(R.id.tv_calories_badge);
        tvStepsBadge      = v.findViewById(R.id.tv_steps_badge);
        tilWeight         = v.findViewById(R.id.til_weight);
        tilCalories       = v.findViewById(R.id.til_calories);
        tilSteps          = v.findViewById(R.id.til_steps);
        etWeight          = v.findViewById(R.id.et_weight);
        etCalories        = v.findViewById(R.id.et_calories);
        etSteps           = v.findViewById(R.id.et_steps);
        chipGroupCalories = v.findViewById(R.id.chip_group_calories);
        chipGroupSteps    = v.findViewById(R.id.chip_group_steps);
        btnSave           = v.findViewById(R.id.btn_save);
        btnBack           = v.findViewById(R.id.btn_back);
        tvMessage         = v.findViewById(R.id.tv_message);
        progressBar       = v.findViewById(R.id.progress_bar);
    }

    // ----------------------------------------------------------------
    // Snap helpers — đảm bảo value luôn align với stepSize của slider
    // ----------------------------------------------------------------
    private float snapWeight(float value) {
        float clamped = Math.max(30f, Math.min(150f, value));
        return Math.round(clamped * 2f) / 2f;
    }

    private int snapCalories(int value) {
        int clamped = Math.max(800, Math.min(4000, value));
        return Math.round(clamped / 50f) * 50;
    }

    private int snapSteps(int value) {
        int clamped = Math.max(1000, Math.min(30000, value));
        return Math.round(clamped / 500f) * 500;
    }

    // ----------------------------------------------------------------
    // Slider → Badge + EditText + Chip
    // ----------------------------------------------------------------
    private void setupSliderListeners() {
        sliderWeight.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser || isUpdating) return;
            isUpdating = true;
            tvWeightBadge.setText(String.format(Locale.getDefault(), "%.1f kg", value));
            etWeight.setText(String.format(Locale.getDefault(), "%.1f", value));
            isUpdating = false;
        });

        sliderCalories.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser || isUpdating) return;
            isUpdating = true;
            int cal = (int) value;
            tvCaloriesBadge.setText(String.format(Locale.getDefault(), "%,d kcal", cal));
            etCalories.setText(String.valueOf(cal));
            syncCaloriesChipInternal(cal);
            isUpdating = false;
        });

        sliderSteps.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser || isUpdating) return;
            isUpdating = true;
            int steps = (int) value;
            tvStepsBadge.setText(String.format(Locale.getDefault(), "%,d", steps));
            etSteps.setText(String.valueOf(steps));
            syncStepsChipInternal(steps);
            isUpdating = false;
        });
    }

    // ----------------------------------------------------------------
    // EditText → Slider + Badge + Chip
    // ----------------------------------------------------------------
    private void setupEditTextListeners() {
        etWeight.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                String raw = s.toString().replace(",", ".").trim();
                if (raw.isEmpty()) return;
                try {
                    float v = Float.parseFloat(raw);
                    float snapped = snapWeight(v);
                    if (snapped >= 30 && snapped <= 150) {
                        isUpdating = true;
                        sliderWeight.setValue(snapped);
                        tvWeightBadge.setText(String.format(Locale.getDefault(), "%.1f kg", snapped));
                        tilWeight.setError(null);
                        isUpdating = false;
                    }
                } catch (NumberFormatException ignored) {}
            }
        });

        etCalories.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                String raw = s.toString().replace(",", ".").trim();
                if (raw.isEmpty()) return;
                try {
                    int cal = Integer.parseInt(raw);
                    int snapped = snapCalories(cal);
                    if (snapped >= 800 && snapped <= 4000) {
                        isUpdating = true;
                        sliderCalories.setValue(snapped);
                        tvCaloriesBadge.setText(String.format(Locale.getDefault(), "%,d kcal", snapped));
                        syncCaloriesChipInternal(snapped);
                        tilCalories.setError(null);
                        isUpdating = false;
                    }
                } catch (NumberFormatException ignored) {}
            }
        });

        etSteps.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                String raw = s.toString().replace(",", ".").trim();
                if (raw.isEmpty()) return;
                try {
                    int steps = Integer.parseInt(raw);
                    int snapped = snapSteps(steps);
                    if (snapped >= 1000 && snapped <= 30000) {
                        isUpdating = true;
                        sliderSteps.setValue(snapped);
                        tvStepsBadge.setText(String.format(Locale.getDefault(), "%,d", snapped));
                        syncStepsChipInternal(snapped);
                        tilSteps.setError(null);
                        isUpdating = false;
                    }
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    // ----------------------------------------------------------------
    // Chip → Slider + EditText + Badge
    // ----------------------------------------------------------------
    private void setupChipListeners() {
        chipGroupCalories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (isUpdating || checkedIds.isEmpty()) return;
            int cal = chipCalToValue(checkedIds.get(0));
            if (cal == -1) return;
            isUpdating = true;
            sliderCalories.setValue(cal);
            etCalories.setText(String.valueOf(cal));
            tvCaloriesBadge.setText(String.format(Locale.getDefault(), "%,d kcal", cal));
            isUpdating = false;
        });

        chipGroupSteps.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (isUpdating || checkedIds.isEmpty()) return;
            int steps = chipStepsToValue(checkedIds.get(0));
            if (steps == -1) return;
            isUpdating = true;
            sliderSteps.setValue(steps);
            etSteps.setText(String.valueOf(steps));
            tvStepsBadge.setText(String.format(Locale.getDefault(), "%,d", steps));
            isUpdating = false;
        });
    }

    // ----------------------------------------------------------------
    // Observe ViewModel
    // ----------------------------------------------------------------
    private void observeViewModel() {
        viewModel.getGoals().observe(getViewLifecycleOwner(), this::populateUI);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!loading);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg == null || msg.isEmpty()) {
                tvMessage.setVisibility(View.GONE);
                return;
            }
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg);
            boolean isError = msg.contains("không") || msg.contains("phải")
                    || msg.contains("hợp lệ");
            tvMessage.setTextColor(getResources().getColor(
                    isError ? R.color.health_error : R.color.health_green_medium, null));
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText("✓ Đã lưu mục tiêu thành công");
                tvMessage.setTextColor(getResources().getColor(R.color.health_green_medium, null));
            }
        });
    }

    // ----------------------------------------------------------------
    // Populate UI từ model — dùng isUpdating để chặn toàn bộ listener
    // ----------------------------------------------------------------
    private void populateUI(HealthGoalsModel model) {
        if (model == null) return;
        isUpdating = true;

        float weight = snapWeight((float) model.getTargetWeight());
        int   cal    = snapCalories(model.getTargetCalories());
        int   steps  = snapSteps(model.getTargetSteps());

        // Weight
        sliderWeight.setValue(weight);
        etWeight.setText(String.format(Locale.getDefault(), "%.1f", weight));
        tvWeightBadge.setText(String.format(Locale.getDefault(), "%.1f kg", weight));

        // Calories
        sliderCalories.setValue(cal);
        etCalories.setText(String.valueOf(cal));
        tvCaloriesBadge.setText(String.format(Locale.getDefault(), "%,d kcal", cal));
        syncCaloriesChipInternal(cal);

        // Steps
        sliderSteps.setValue(steps);
        etSteps.setText(String.valueOf(steps));
        tvStepsBadge.setText(String.format(Locale.getDefault(), "%,d", steps));
        syncStepsChipInternal(steps);

        isUpdating = false;
    }

    // ----------------------------------------------------------------
    // Internal chip sync — chỉ gọi khi isUpdating đã được set true
    // ----------------------------------------------------------------
    private void syncCaloriesChipInternal(int cal) {
        int chipId = -1;
        if      (cal == 1200) chipId = R.id.chip_cal_1200;
        else if (cal == 1500) chipId = R.id.chip_cal_1500;
        else if (cal == 2000) chipId = R.id.chip_cal_2000;
        else if (cal == 2500) chipId = R.id.chip_cal_2500;
        else if (cal == 3000) chipId = R.id.chip_cal_3000;

        chipGroupCalories.clearCheck();
        if (chipId != -1) chipGroupCalories.check(chipId);
    }

    private void syncStepsChipInternal(int steps) {
        int chipId = -1;
        if      (steps == 5000)  chipId = R.id.chip_steps_5000;
        else if (steps == 7000)  chipId = R.id.chip_steps_7000;
        else if (steps == 8000)  chipId = R.id.chip_steps_8000;
        else if (steps == 10000) chipId = R.id.chip_steps_10000;
        else if (steps == 15000) chipId = R.id.chip_steps_15000;

        chipGroupSteps.clearCheck();
        if (chipId != -1) chipGroupSteps.check(chipId);
    }

    private int chipCalToValue(int chipId) {
        if      (chipId == R.id.chip_cal_1200) return 1200;
        else if (chipId == R.id.chip_cal_1500) return 1500;
        else if (chipId == R.id.chip_cal_2000) return 2000;
        else if (chipId == R.id.chip_cal_2500) return 2500;
        else if (chipId == R.id.chip_cal_3000) return 3000;
        return -1;
    }

    private int chipStepsToValue(int chipId) {
        if      (chipId == R.id.chip_steps_5000)  return 5000;
        else if (chipId == R.id.chip_steps_7000)  return 7000;
        else if (chipId == R.id.chip_steps_8000)  return 8000;
        else if (chipId == R.id.chip_steps_10000) return 10000;
        else if (chipId == R.id.chip_steps_15000) return 15000;
        return -1;
    }

    // ----------------------------------------------------------------
    // TextWatcher helper
    // ----------------------------------------------------------------
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c)     {}
    }
}