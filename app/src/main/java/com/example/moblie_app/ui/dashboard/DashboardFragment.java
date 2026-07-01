package com.example.moblie_app.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;

import com.example.moblie_app.MainActivity;
import com.example.moblie_app.R;
import com.example.moblie_app.databinding.FragmentDashboardBinding;
import com.example.moblie_app.model.ActivityLogModel;
import com.example.moblie_app.model.HealthGoalsModel;
import com.example.moblie_app.model.MealEntryModel;
import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.model.WaterLogModel;
import com.example.moblie_app.model.WeightLogModel;
import com.example.moblie_app.repository.ActivityRepository;
import com.example.moblie_app.repository.FoodDiaryRepository;
import com.example.moblie_app.repository.HealthGoalsRepository;
import com.example.moblie_app.repository.ProfileRepository;
import com.example.moblie_app.repository.WaterRepository;
import com.example.moblie_app.utils.BmiCalculator;
import com.example.moblie_app.utils.Constants;
import com.example.moblie_app.utils.DateUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

/**
 * DashboardFragment - màn hình tổng quan sau đăng nhập.
 * Hiển thị các biểu đồ tròn (ring chart) tiến độ Calo / Nước / Bước chân / Cân nặng so với mục tiêu.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    private HealthGoalsRepository healthGoalsRepository;
    private FoodDiaryRepository foodDiaryRepository;
    private WaterRepository waterRepository;
    private ActivityRepository activityRepository;
    private ProfileRepository profileRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = "bạn";
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            name = user.getDisplayName();
        } else if (user != null && user.getEmail() != null) {
            name = user.getEmail();
        }

        binding.tvSubtitle.setText("Chào bạn, " + name);
        binding.tvOverviewDate.setText("Hôm nay, " + DateUtils.getTodayDisplay());

        healthGoalsRepository = new HealthGoalsRepository(requireContext());
        foodDiaryRepository = new FoodDiaryRepository();
        waterRepository = new WaterRepository();
        activityRepository = new ActivityRepository(requireContext());
        profileRepository = new ProfileRepository(requireContext());

        binding.btnOpenFoodDiary.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_food_diary));
        binding.btnOpenProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_profile));
        binding.btnOpenGoals.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_goals));
        binding.btnOpenActivity.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_activity));
        binding.btnOpenCalorieChart.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_calorie_chart));
        binding.btnOpenFavorites.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_search_food));
        binding.btnOpenSleep.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_sleep));
        binding.btnOpenSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_settings));
        binding.btnLogout.setOnClickListener(v -> logout());

        loadOverview();
    }

    /**
     * Tải song song: mục tiêu, calo hôm nay, nước hôm nay, bước chân hôm nay, cân nặng/BMI gần nhất.
     * Mỗi phần độc lập, lỗi ở phần này không chặn phần khác.
     */
    private void loadOverview() {
        String todayKey = DateUtils.getTodayKey();

        // --- Mục tiêu (calo, bước chân, cân nặng) ---
        MutableLiveData<HealthGoalsModel> goalsLive = new MutableLiveData<>();
        MutableLiveData<String> goalsError = new MutableLiveData<>();
        MutableLiveData<Boolean> goalsLoading = new MutableLiveData<>();
        healthGoalsRepository.loadGoals(goalsLive, goalsError, goalsLoading);
        goalsLive.observe(getViewLifecycleOwner(), goals -> {
            if (goals == null || binding == null) return;

            int targetCalories = goals.getTargetCalories() > 0
                    ? goals.getTargetCalories() : HealthGoalsModel.DEFAULT_CALORIES;
            int targetSteps = goals.getTargetSteps() > 0
                    ? goals.getTargetSteps() : HealthGoalsModel.DEFAULT_STEPS;
            double targetWeight = goals.getTargetWeight() > 0
                    ? goals.getTargetWeight() : HealthGoalsModel.DEFAULT_WEIGHT;

            loadCaloriesToday(todayKey, targetCalories);
            loadStepsToday(todayKey, targetSteps);
            loadWeightAndBmi(targetWeight);
        });

        // --- Nước uống hôm nay (mục tiêu lấy từ hồ sơ, mặc định 2 lít) ---
        MutableLiveData<UserModel> profileLive = new MutableLiveData<>();
        MutableLiveData<String> profileError = new MutableLiveData<>();
        profileRepository.getProfile(profileLive, profileError);
        profileLive.observe(getViewLifecycleOwner(), userModel -> {
            double targetWaterMl = (userModel != null && userModel.getTargetWater() > 0)
                    ? userModel.getTargetWater() * 1000.0 : 2000.0;
            loadWaterToday(todayKey, targetWaterMl);
        });
        // Nếu chưa có hồ sơ (lỗi/đăng nhập lần đầu) vẫn hiển thị với mục tiêu mặc định 2L
        profileError.observe(getViewLifecycleOwner(), err -> {
            if (err != null) loadWaterToday(todayKey, 2000.0);
        });
    }

    private void loadCaloriesToday(String todayKey, int targetCalories) {
        foodDiaryRepository.loadEntries(todayKey, new FoodDiaryRepository.Callback() {
            @Override
            public void onSuccess(List<MealEntryModel> entries) {
                if (binding == null) return;
                double totalCalories = 0;
                for (MealEntryModel m : entries) totalCalories += m.getCalories();
                binding.ringCalories.setRingColor("#E67E22");
                binding.ringCalories.setProgress(
                        totalCalories, targetCalories,
                        String.format(Locale.getDefault(), "%.0f", totalCalories), "kcal");
                binding.tvCaloriesLabel.setText(String.format(Locale.getDefault(),
                        "%.0f/%d kcal", totalCalories, targetCalories));
            }

            @Override
            public void onError(String error) {
                if (binding == null) return;
                binding.ringCalories.setRingColor("#E67E22");
                binding.ringCalories.setProgress(0, targetCalories, "0", "kcal");
                binding.tvCaloriesLabel.setText("0/" + targetCalories + " kcal");
            }
        });
    }

    private void loadWaterToday(String todayKey, double targetWaterMl) {
        MutableLiveData<List<WaterLogModel>> waterLive = new MutableLiveData<>();
        MutableLiveData<String> waterError = new MutableLiveData<>();
        waterRepository.loadWaterLogs(todayKey, waterLive, waterError);
        waterLive.observe(getViewLifecycleOwner(), logs -> {
            if (binding == null) return;
            int totalMl = 0;
            if (logs != null) {
                for (WaterLogModel log : logs) totalMl += log.getAmountMl();
            }
            double totalLiters = totalMl / 1000.0;
            double targetLiters = targetWaterMl / 1000.0;
            binding.ringWater.setRingColor("#3498DB");
            binding.ringWater.setProgress(
                    totalMl, targetWaterMl,
                    String.format(Locale.getDefault(), "%.1f", totalLiters), "lít");
            binding.tvWaterLabel.setText(String.format(Locale.getDefault(),
                    "%.1f/%.1f lít", totalLiters, targetLiters));
        });
    }

    private void loadStepsToday(String todayKey, int targetSteps) {
        MutableLiveData<List<ActivityLogModel>> activityLive = new MutableLiveData<>();
        MutableLiveData<String> activityError = new MutableLiveData<>();
        activityRepository.loadActivityLogs(activityLive, activityError);
        activityLive.observe(getViewLifecycleOwner(), logs -> {
            if (binding == null) return;
            int steps = 0;
            if (logs != null) {
                for (ActivityLogModel log : logs) {
                    if (todayKey.equals(log.getDateKey())
                            && Constants.ACTIVITY_STEPS.equals(log.getActivityType())) {
                        steps = log.getStepCount();
                        break;
                    }
                }
            }
            binding.ringSteps.setRingColor("#10B981");
            binding.ringSteps.setProgress(
                    steps, targetSteps, String.valueOf(steps), "bước");
            binding.tvStepsLabel.setText(steps + "/" + targetSteps + " bước");
        });
    }

    private void loadWeightAndBmi(double targetWeight) {
        MutableLiveData<List<WeightLogModel>> weightLive = new MutableLiveData<>();
        MutableLiveData<String> weightError = new MutableLiveData<>();
        activityRepository.loadWeightLogs(weightLive, weightError);
        weightLive.observe(getViewLifecycleOwner(), logs -> {
            if (binding == null) return;
            binding.ringWeight.setRingColor("#8E44AD");

            if (logs == null || logs.isEmpty()) {
                binding.ringWeight.setProgress(0, targetWeight, "--", "kg");
                binding.tvWeightLabel.setText("Chưa có dữ liệu · mục tiêu "
                        + String.format(Locale.getDefault(), "%.1f", targetWeight) + " kg");
                binding.tvBmiBadge.setText("BMI -- · Chưa có dữ liệu");
                return;
            }

            WeightLogModel latest = logs.get(0); // đã sort giảm dần theo timestamp
            double currentWeight = latest.getWeightKg();

            // Tiến độ: càng gần mục tiêu càng đầy vòng tròn (không quan tâm tăng hay giảm cân)
            double diff = Math.abs(targetWeight - currentWeight);
            double progressTowardGoal = targetWeight > 0
                    ? Math.max(0, 1 - (diff / targetWeight)) : 0;

            binding.ringWeight.setProgress(
                    progressTowardGoal * 100, 100,
                    String.format(Locale.getDefault(), "%.1f", currentWeight), "kg");
            binding.tvWeightLabel.setText(String.format(Locale.getDefault(),
                    "%.1f kg · mục tiêu %.1f kg", currentWeight, targetWeight));

            double bmi = latest.getBmi() > 0
                    ? latest.getBmi()
                    : BmiCalculator.calculate(currentWeight, latest.getHeightCm());
            String category = latest.getBmiCategory() != null
                    ? latest.getBmiCategory() : BmiCalculator.category(bmi);
            binding.tvBmiBadge.setText("BMI " + BmiCalculator.format(bmi) + " · " + category);
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finishAffinity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}