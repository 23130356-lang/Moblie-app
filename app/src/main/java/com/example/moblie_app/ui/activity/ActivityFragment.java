package com.example.moblie_app.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moblie_app.databinding.FragmentActivityBinding;
import com.example.moblie_app.model.DailyActivityStat;
import com.example.moblie_app.model.WeeklyActivityStats;
import com.example.moblie_app.model.WeightLogModel;
import com.example.moblie_app.repository.GoogleFitRepository;
import com.example.moblie_app.service.StepCounterService;
import com.example.moblie_app.utils.ActivityCalculator;
import com.example.moblie_app.utils.BmiCalculator;
import com.example.moblie_app.utils.Constants;
import com.example.moblie_app.utils.StepGoalTracker;
import com.example.moblie_app.viewmodel.ActivityViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ActivityFragment extends Fragment {

    private static final int REQUEST_ACTIVITY_PERMISSIONS = 3201;
    private static final int REQUEST_GOOGLE_FIT = 3202;
    private static final int DEFAULT_STEP_GOAL = 8000;

    private FragmentActivityBinding binding;
    private ActivityViewModel viewModel;
    private ActivityLogAdapter activityLogAdapter;
    private WeightLogAdapter weightLogAdapter;
    private GoogleFitRepository googleFitRepository;
    private FitnessOptions fitnessOptions;
    private boolean receiverRegistered;

    private final List<String> activityTypes = Arrays.asList(
            Constants.ACTIVITY_RUNNING,
            Constants.ACTIVITY_WALKING,
            Constants.ACTIVITY_CYCLING,
            Constants.ACTIVITY_SWIMMING,
            Constants.ACTIVITY_YOGA,
            Constants.ACTIVITY_GYM);

    private final BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (StepCounterService.ACTION_STEP_UPDATE.equals(intent.getAction())) {
                int steps = intent.getIntExtra(StepCounterService.EXTRA_STEPS, 0);
                viewModel.setLiveSteps(steps);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(
                this,
                new ActivityViewModel.Factory(requireContext()))
                .get(ActivityViewModel.class);
        googleFitRepository = new GoogleFitRepository();
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        setupLists();
        setupSpinner();
        setupChart(binding.barChart);
        setupClicks();
        observeViewModel();
        viewModel.loadAll();
    }

    @Override
    public void onStart() {
        super.onStart();
        registerStepReceiver();
    }

    @Override
    public void onStop() {
        unregisterStepReceiver();
        super.onStop();
    }

    private void setupLists() {
        activityLogAdapter = new ActivityLogAdapter(log -> viewModel.deleteActivityLog(log.getId()));
        binding.rvActivityLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvActivityLogs.setAdapter(activityLogAdapter);

        weightLogAdapter = new WeightLogAdapter(log -> viewModel.deleteWeightLog(log.getId()));
        binding.rvWeightLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWeightLogs.setAdapter(weightLogAdapter);
    }

    private void setupSpinner() {
        List<String> labels = new ArrayList<>();
        for (String type : activityTypes) {
            labels.add(ActivityCalculator.displayName(type));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerActivityType.setAdapter(adapter);
    }

    private void setupClicks() {
        binding.btnStartStepCounter.setOnClickListener(v -> startStepCounter());
        binding.btnSaveSteps.setOnClickListener(v -> viewModel.saveCurrentSteps());
        binding.btnSyncGoogleFit.setOnClickListener(v -> syncGoogleFitSteps());
        binding.btnAddWorkout.setOnClickListener(v -> addWorkout());
        binding.btnAddWeight.setOnClickListener(v -> addWeight());
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                binding.tvError.setText(error);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getLiveSteps().observe(getViewLifecycleOwner(), this::updateStepViews);

        viewModel.getActivityLogs().observe(getViewLifecycleOwner(), logs -> {
            activityLogAdapter.submitList(logs);
            binding.tvEmptyActivities.setVisibility(logs == null || logs.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });

        viewModel.getWeightLogs().observe(getViewLifecycleOwner(), logs -> {
            weightLogAdapter.submitList(logs);
            binding.tvEmptyWeights.setVisibility(logs == null || logs.isEmpty()
                    ? View.VISIBLE : View.GONE);
            updateLatestBmi(logs);
        });

        viewModel.getWeeklyStats().observe(getViewLifecycleOwner(), stats -> {
            updateWeeklySummary(stats);
            updateChart(stats);
        });

        viewModel.getActionDone().observe(getViewLifecycleOwner(), done -> {
            if (Boolean.TRUE.equals(done)) {
                clearFormFields();
                Toast.makeText(requireContext(), "Đã lưu dữ liệu", Toast.LENGTH_SHORT).show();
                binding.tvError.setVisibility(View.GONE);
                viewModel.onActionHandled();
            }
        });
    }

    private void addWorkout() {
        int selectedPosition = binding.spinnerActivityType.getSelectedItemPosition();
        String activityType = activityTypes.get(Math.max(0, selectedPosition));
        viewModel.addWorkout(
                activityType,
                textOf(binding.etDuration),
                textOf(binding.etWorkoutWeight),
                textOf(binding.etWorkoutNote));
    }

    private void addWeight() {
        viewModel.addWeightLog(
                textOf(binding.etWeight),
                textOf(binding.etHeight),
                textOf(binding.etWeightNote));
    }

    private void startStepCounter() {
        if (!hasActivityRecognitionPermission()) {
            requestActivityPermissions();
            return;
        }
        Intent intent = new Intent(requireContext(), StepCounterService.class);
        ContextCompat.startForegroundService(requireContext(), intent);
        Toast.makeText(requireContext(), "Đang theo dõi bước chân", Toast.LENGTH_SHORT).show();
    }

    private boolean hasActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true;
        }
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestActivityPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        requestPermissions(permissions.toArray(new String[0]), REQUEST_ACTIVITY_PERMISSIONS);
    }

    private void syncGoogleFitSteps() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                requireContext(),
                fitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, REQUEST_GOOGLE_FIT, account, fitnessOptions);
            return;
        }

        googleFitRepository.readTodaySteps(requireContext(), account,
                new GoogleFitRepository.StepCallback() {
                    @Override
                    public void onSuccess(int steps) {
                        viewModel.saveStepsFromGoogleFit(steps);
                    }

                    @Override
                    public void onError(String message) {
                        binding.tvError.setText("Không đọc được Google Fit: " + message);
                        binding.tvError.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GOOGLE_FIT && resultCode == Activity.RESULT_OK) {
            syncGoogleFitSteps();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_ACTIVITY_PERMISSIONS) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepCounter();
        } else {
            binding.tvError.setText("Cần quyền nhận diện hoạt động để đếm bước chân.");
            binding.tvError.setVisibility(View.VISIBLE);
        }
    }

    private void registerStepReceiver() {
        if (receiverRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter(StepCounterService.ACTION_STEP_UPDATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(stepReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(stepReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void unregisterStepReceiver() {
        if (!receiverRegistered) {
            return;
        }
        requireContext().unregisterReceiver(stepReceiver);
        receiverRegistered = false;
    }

    private void updateStepViews(int steps) {
        int progress = StepGoalTracker.progressPercent(steps, DEFAULT_STEP_GOAL);
        int remaining = StepGoalTracker.remainingSteps(steps, DEFAULT_STEP_GOAL);
        binding.tvStepCount.setText(String.format(Locale.getDefault(), "%,d bước", steps));
        binding.tvStepGoal.setText(String.format(Locale.getDefault(),
                "Đạt %d%% mục tiêu · còn %,d bước", progress, remaining));
        binding.progressSteps.setProgress(progress);
    }

    private void updateLatestBmi(List<WeightLogModel> logs) {
        if (logs == null || logs.isEmpty()) {
            binding.tvBmiSummary.setText("Chưa có dữ liệu cân nặng.");
            return;
        }
        WeightLogModel latest = logs.get(0);
        binding.tvBmiSummary.setText(String.format(Locale.getDefault(),
                "Gần nhất: %.1f kg · BMI %s · %s",
                latest.getWeightKg(),
                BmiCalculator.format(latest.getBmi()),
                latest.getBmiCategory()));
        if (textOf(binding.etWorkoutWeight).isEmpty()) {
            binding.etWorkoutWeight.setText(String.format(Locale.US, "%.1f", latest.getWeightKg()));
        }
    }

    private void updateWeeklySummary(WeeklyActivityStats stats) {
        if (stats == null) {
            binding.tvWeeklySummary.setText("Chưa có thống kê tuần.");
            return;
        }
        binding.tvWeeklySummary.setText(String.format(Locale.getDefault(),
                "Tổng: %,d bước · %.2f km · %.0f kcal · %d phút tập",
                stats.getTotalSteps(),
                stats.getTotalDistanceKm(),
                stats.getTotalCaloriesBurned(),
                stats.getTotalDurationMinutes()));
    }

    private void setupChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.setFitBars(true);
        chart.setNoDataText("Chưa có dữ liệu bước chân");

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
    }

    private void updateChart(WeeklyActivityStats stats) {
        if (stats == null || stats.getDailyStats() == null) {
            binding.barChart.clear();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<DailyActivityStat> dailyStats = stats.getDailyStats();
        for (int i = 0; i < dailyStats.size(); i++) {
            DailyActivityStat day = dailyStats.get(i);
            entries.add(new BarEntry(i, day.getSteps()));
            labels.add(day.getLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Bước chân");
        dataSet.setColor(Color.rgb(46, 125, 50));
        dataSet.setValueTextColor(Color.rgb(60, 60, 60));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.55f);

        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChart.setData(data);
        binding.barChart.invalidate();
    }

    private void clearFormFields() {
        binding.etDuration.setText("");
        binding.etWorkoutNote.setText("");
        binding.etWeight.setText("");
        binding.etHeight.setText("");
        binding.etWeightNote.setText("");
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        unregisterStepReceiver();
        binding = null;
        super.onDestroyView();
    }
}
