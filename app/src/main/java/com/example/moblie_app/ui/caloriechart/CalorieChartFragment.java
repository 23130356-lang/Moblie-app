package com.example.moblie_app.ui.caloriechart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moblie_app.R;
import com.example.moblie_app.databinding.FragmentCalorieChartBinding;
import com.example.moblie_app.viewmodel.CalorieChartViewModel;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalorieChartFragment extends Fragment {

    private FragmentCalorieChartBinding binding;
    private CalorieChartViewModel viewModel;
    private boolean isWeekMode = true;

    private int colorGreenPrimary;
    private int colorGreenMedium;
    private int colorTextSecondary;
    private int colorCalorie;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalorieChartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        colorGreenPrimary  = ContextCompat.getColor(requireContext(), R.color.health_green_primary);
        colorGreenMedium   = ContextCompat.getColor(requireContext(), R.color.health_green_medium);
        colorTextSecondary = ContextCompat.getColor(requireContext(), R.color.health_text_secondary);
        colorCalorie       = ContextCompat.getColor(requireContext(), R.color.health_calorie);

        viewModel = new ViewModelProvider(
                this,
                new CalorieChartViewModel.Factory(requireContext()))
                .get(CalorieChartViewModel.class);

        setupViews();
        observeViewModel();
        viewModel.loadData();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v).navigateUp());

        setupBarChart();
        setupLineChart();

        binding.tabPeriod.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                isWeekMode = tab.getPosition() == 0;
                updateChartVisibility();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                binding.tvError.setText(error);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });

        viewModel.getCalorieGoal().observe(getViewLifecycleOwner(), goal -> {
            String text = String.format(Locale.getDefault(), "%d kcal", goal);
            binding.tvCalorieGoal.setText(text);
            updateLimitLines(goal);
        });

        viewModel.getWeekEntries().observe(getViewLifecycleOwner(), entries -> {
            List<String> labels = viewModel.getWeekLabels().getValue();
            if (labels == null) labels = new ArrayList<>();
            updateBarChart(entries, labels);
        });

        viewModel.getMonthEntries().observe(getViewLifecycleOwner(), entries -> {
            List<String> labels = viewModel.getMonthLabels().getValue();
            if (labels == null) labels = new ArrayList<>();
            updateLineChart(entries, labels);
        });
    }

    private void setupBarChart() {
        binding.barChartWeek.getDescription().setEnabled(false);
        binding.barChartWeek.getLegend().setEnabled(false);
        binding.barChartWeek.getAxisRight().setEnabled(false);
        binding.barChartWeek.getAxisLeft().setAxisMinimum(0f);
        binding.barChartWeek.setFitBars(true);
        binding.barChartWeek.setNoDataText("Chưa có dữ liệu tuần này");
        binding.barChartWeek.setDrawValueAboveBar(true);
        binding.barChartWeek.setPinchZoom(false);
        binding.barChartWeek.setScaleEnabled(false);

        XAxis xAxis = binding.barChartWeek.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);
        binding.barChartWeek.getAxisLeft().setDrawGridLines(false);
    }

    private void updateBarChart(List<BarEntry> entries, List<String> labels) {
        if (entries == null || entries.isEmpty()) {
            binding.barChartWeek.clear();
            binding.barChartWeek.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Calo");
        dataSet.setColor(colorGreenPrimary);
        dataSet.setValueTextColor(colorTextSecondary);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.55f);

        binding.barChartWeek.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChartWeek.setData(data);
        binding.barChartWeek.invalidate();
    }

    private void setupLineChart() {
        binding.lineChartMonth.getDescription().setEnabled(false);
        binding.lineChartMonth.getLegend().setEnabled(false);
        binding.lineChartMonth.getAxisRight().setEnabled(false);
        binding.lineChartMonth.getAxisLeft().setAxisMinimum(0f);
        binding.lineChartMonth.setNoDataText("Chưa có dữ liệu tháng này");
        binding.lineChartMonth.setPinchZoom(false);
        binding.lineChartMonth.setScaleEnabled(false);

        XAxis xAxis = binding.lineChartMonth.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);
        xAxis.setLabelCount(6, true);
        binding.lineChartMonth.getAxisLeft().setDrawGridLines(false);
    }

    private void updateLineChart(List<Entry> entries, List<String> labels) {
        if (entries == null || entries.isEmpty()) {
            binding.lineChartMonth.clear();
            binding.lineChartMonth.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Calo");
        dataSet.setColor(colorGreenPrimary);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(colorGreenMedium);
        dataSet.setCircleRadius(3f);
        dataSet.setValueTextColor(colorTextSecondary);
        dataSet.setValueTextSize(9f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(colorGreenPrimary & 0x00FFFFFF | 0x28000000);
        dataSet.setDrawCircles(true);
        dataSet.setDrawValues(false);

        LineData data = new LineData(dataSet);

        binding.lineChartMonth.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.lineChartMonth.setData(data);
        binding.lineChartMonth.invalidate();
    }

    private void updateLimitLines(int goalCalories) {
        LimitLine limitLine = new LimitLine((float) goalCalories, "Mục tiêu: " + goalCalories + " kcal");
        limitLine.setLineColor(colorCalorie);
        limitLine.setLineWidth(2f);
        limitLine.setTextColor(colorCalorie);
        limitLine.setTextSize(11f);
        limitLine.enableDashedLine(10f, 5f, 0f);

        binding.barChartWeek.getAxisLeft().removeAllLimitLines();
        binding.barChartWeek.getAxisLeft().addLimitLine(limitLine);
        binding.lineChartMonth.getAxisLeft().removeAllLimitLines();
        binding.lineChartMonth.getAxisLeft().addLimitLine(limitLine);
    }

    private void updateChartVisibility() {
        binding.barChartWeek.setVisibility(isWeekMode ? View.VISIBLE : View.GONE);
        binding.lineChartMonth.setVisibility(isWeekMode ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
