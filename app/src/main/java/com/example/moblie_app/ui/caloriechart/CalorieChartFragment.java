package com.example.moblie_app.ui.caloriechart;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.moblie_app.databinding.FragmentCalorieChartBinding;
import com.example.moblie_app.viewmodel.CalorieChartViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Locale;

/**
 * CalorieChartFragment - Hiển thị biểu đồ calo tiêu thụ theo tuần/tháng.
 */
public class CalorieChartFragment extends Fragment {

    private FragmentCalorieChartBinding binding;
    private CalorieChartViewModel viewModel;

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

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this, new CalorieChartViewModel.Factory(requireContext()))
                .get(CalorieChartViewModel.class);

        setupCharts();
        setupListeners();
        observeViewModel();

        // Tải dữ liệu
        viewModel.loadData();
    }

    private void setupCharts() {
        // Biểu đồ cột cho tuần
        configureBarChart(binding.barChartWeek);
        // Biểu đồ đường cho tháng
        configureLineChart(binding.lineChartMonth);
    }

    private void configureBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.setFitBars(true);
        chart.setNoDataText("Chưa có dữ liệu tuần");

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
    }

    private void configureLineChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.setNoDataText("Chưa có dữ liệu tháng");

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
    }

    private void setupListeners() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Chuyển đổi giữa tuần và tháng
        binding.tabPeriod.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.barChartWeek.setVisibility(View.VISIBLE);
                    binding.lineChartMonth.setVisibility(View.GONE);
                } else {
                    binding.barChartWeek.setVisibility(View.GONE);
                    binding.lineChartMonth.setVisibility(View.VISIBLE);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding != null) {
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (binding != null) {
                if (error != null && !error.isEmpty()) {
                    binding.tvError.setText(error);
                    binding.tvError.setVisibility(View.VISIBLE);
                } else {
                    binding.tvError.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getCalorieGoal().observe(getViewLifecycleOwner(), goal -> {
            if (binding != null) {
                binding.tvCalorieGoal.setText(String.format(Locale.getDefault(), "%d kcal", goal));
            }
        });

        viewModel.getWeekEntries().observe(getViewLifecycleOwner(), entries -> {
            if (binding != null) {
                updateWeekChart(entries, viewModel.getWeekLabels().getValue());
            }
        });

        viewModel.getMonthEntries().observe(getViewLifecycleOwner(), entries -> {
            if (binding != null) {
                updateMonthChart(entries, viewModel.getMonthLabels().getValue());
            }
        });
    }

    private void updateWeekChart(List<BarEntry> entries, List<String> labels) {
        if (entries == null || entries.isEmpty() || binding == null) return;

        BarDataSet dataSet = new BarDataSet(entries, "Calo hàng ngày");
        dataSet.setColor(Color.rgb(76, 175, 80)); // health_green_primary
        dataSet.setValueTextColor(Color.GRAY);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        if (labels != null) {
            binding.barChartWeek.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        }
        binding.barChartWeek.setData(data);
        binding.barChartWeek.invalidate();
    }

    private void updateMonthChart(List<Entry> entries, List<String> labels) {
        if (entries == null || entries.isEmpty() || binding == null) return;

        LineDataSet dataSet = new LineDataSet(entries, "Calo hàng tháng");
        dataSet.setColor(Color.rgb(76, 175, 80));
        dataSet.setCircleColor(Color.rgb(76, 175, 80));
        dataSet.setLineWidth(2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.argb(50, 76, 175, 80));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(dataSet);
        data.setValueTextSize(9f);

        if (labels != null) {
            binding.lineChartMonth.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        }
        binding.lineChartMonth.setData(data);
        binding.lineChartMonth.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
