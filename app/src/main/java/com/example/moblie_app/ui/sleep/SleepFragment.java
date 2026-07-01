package com.example.moblie_app.ui.sleep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moblie_app.databinding.FragmentSleepBinding;
import com.example.moblie_app.model.SleepLogModel;
import com.example.moblie_app.utils.SleepAnalyzer;
import com.example.moblie_app.viewmodel.SleepViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * SleepFragment – Ghi nhật ký giấc ngủ, biểu đồ 7 ngày, theo dõi nước uống.
 */
public class SleepFragment extends Fragment {

    private FragmentSleepBinding binding;
    private SleepViewModel viewModel;
    private SleepLogAdapter sleepLogAdapter;
    private WaterLogAdapter waterLogAdapter;

    // Lưu giờ người dùng chọn để truyền vào ViewModel
    private String selectedBedTime  = "";
    private String selectedWakeTime = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSleepBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(
                this,
                new SleepViewModel.Factory(requireActivity().getApplication()))
                .get(SleepViewModel.class);

        setupRecyclerView();
        setupQualitySpinner();
        setupChart(binding.lineChartSleep);
        setupClicks();
        observeViewModel();
        viewModel.loadAll();
    }

    // ─── Setup ───────────────────────────────────────────────────

    private void setupRecyclerView() {
        sleepLogAdapter = new SleepLogAdapter(log -> viewModel.deleteSleepLog(log.getId()));
        binding.rvSleepLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSleepLogs.setAdapter(sleepLogAdapter);

        waterLogAdapter = new WaterLogAdapter(log -> viewModel.deleteWaterLog(log.getId()));
        binding.rvWaterLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWaterLogs.setAdapter(waterLogAdapter);
    }

    private void setupQualitySpinner() {
        String[] qualityOptions = {
                "Tự động (từ thời lượng)",
                "1 – Rất tệ",
                "2 – Kém",
                "3 – Trung bình",
                "4 – Tốt",
                "5 – Rất tốt"
        };
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                qualityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerQuality.setAdapter(adapter);
    }

    private void setupChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(12f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setLabelCount(5);
        chart.setNoDataText("Chưa có dữ liệu giấc ngủ");
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(android.graphics.Color.parseColor("#9CA3AF"));
    }

    private void setupClicks() {
        // Nút Back
        binding.btnBack.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v).popBackStack());

        // TimePicker – giờ đi ngủ
        binding.btnPickBedTime.setOnClickListener(v -> showTimePicker(true));

        // TimePicker – giờ thức dậy
        binding.btnPickWakeTime.setOnClickListener(v -> showTimePicker(false));

        // Lưu nhật ký giấc ngủ
        binding.btnAddSleepLog.setOnClickListener(v -> addSleepLog());

        // Uống nước – nút nhanh
        binding.btnWater250.setOnClickListener(v -> viewModel.addWaterQuick(250));
        binding.btnWater350.setOnClickListener(v -> viewModel.addWaterQuick(350));
        binding.btnWater500.setOnClickListener(v -> viewModel.addWaterQuick(500));

        // Uống nước – nhập tuỳ chỉnh
        binding.btnAddWater.setOnClickListener(v -> {
            String text = binding.etWaterAmount.getText() == null
                    ? "" : binding.etWaterAmount.getText().toString().trim();
            viewModel.addWaterLog(text);
        });

        // Đặt nhắc uống nước (AlarmManager)
        binding.btnSetWaterReminder.setOnClickListener(v -> scheduleWaterReminder());

        // Đặt nhắc đi ngủ
        binding.btnSetSleepReminder.setOnClickListener(v -> scheduleSleepReminder());
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                binding.tvError.setText(error);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getSleepLogs().observe(getViewLifecycleOwner(), logs -> {
            sleepLogAdapter.submitList(logs);
            binding.tvEmptySleep.setVisibility(
                    logs == null || logs.isEmpty() ? View.VISIBLE : View.GONE);
            // Tắt loading khi data về
            binding.progressBar.setVisibility(View.GONE);
        });

        viewModel.getRecentSleepLogs().observe(getViewLifecycleOwner(), logs -> {
            updateChart(logs);
            updateSleepSummary(logs);
        });

        viewModel.getWaterLogs().observe(getViewLifecycleOwner(), logs -> {
            viewModel.refreshTotalWater(logs);
            waterLogAdapter.submitList(logs);
            binding.tvEmptyWater.setVisibility(
                    logs == null || logs.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getTotalWaterToday().observe(getViewLifecycleOwner(), total -> {
            binding.tvWaterTotal.setText(String.format(Locale.getDefault(),
                    "%d ml / 2000 ml", total));
            int progress = Math.min(100, (int) ((total / 2000.0) * 100));
            binding.progressWater.setProgress(progress);
        });

        viewModel.getActionDone().observe(getViewLifecycleOwner(), done -> {
            if (Boolean.TRUE.equals(done)) {
                clearSleepForm();
                if (binding.etWaterAmount.getText() != null) {
                    binding.etWaterAmount.setText("");
                }
                Toast.makeText(requireContext(), "Đã lưu", Toast.LENGTH_SHORT).show();
                binding.tvError.setVisibility(View.GONE);
                viewModel.onActionHandled();
            }
        });
    }

    // ─── Actions ─────────────────────────────────────────────────

    private void showTimePicker(boolean isBedTime) {
        Calendar calendar = Calendar.getInstance();
        int hour   = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (picker, h, m) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            if (isBedTime) {
                selectedBedTime = time;
                binding.btnPickBedTime.setText("Ngủ: " + time);
            } else {
                selectedWakeTime = time;
                binding.btnPickWakeTime.setText("Thức: " + time);
            }
        }, hour, minute, true).show();
    }

    private void addSleepLog() {
        // Spinner vị trí 0 = "Tự động", 1–5 = điểm tương ứng
        int quality = binding.spinnerQuality.getSelectedItemPosition(); // 0 = tự động

        String note = binding.etSleepNote.getText() == null
                ? "" : binding.etSleepNote.getText().toString().trim();

        viewModel.addSleepLog(selectedBedTime, selectedWakeTime, quality, note);
    }

    private void clearSleepForm() {
        selectedBedTime  = "";
        selectedWakeTime = "";
        binding.btnPickBedTime.setText("Chọn giờ ngủ");
        binding.btnPickWakeTime.setText("Chọn giờ thức");
        binding.etSleepNote.setText("");
        binding.spinnerQuality.setSelection(0);
    }

    // ─── Chart ───────────────────────────────────────────────────

    private void updateChart(List<SleepLogModel> logs) {
        if (logs == null || logs.isEmpty()) {
            binding.lineChartSleep.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < logs.size(); i++) {
            SleepLogModel log = logs.get(i);
            entries.add(new Entry(i, (float) log.getDurationHours()));
            // Fix NPE: kiểm tra dateKey trước khi substring
            String dateKey = log.getDateKey();
            labels.add(dateKey != null && dateKey.length() >= 7
                    ? dateKey.substring(5) : "??");
        }

        LineDataSet dataSet = new LineDataSet(entries, "Giờ ngủ");
        dataSet.setColor(android.graphics.Color.parseColor("#10B981"));
        dataSet.setCircleColor(android.graphics.Color.parseColor("#10B981"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(android.graphics.Color.parseColor("#526B60"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(30);
        dataSet.setFillColor(android.graphics.Color.parseColor("#10B981"));

        binding.lineChartSleep.getXAxis()
                .setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.lineChartSleep.setData(new LineData(dataSet));
        binding.lineChartSleep.invalidate();
    }

    private void updateSleepSummary(List<SleepLogModel> logs) {
        if (logs == null || logs.isEmpty()) {
            binding.tvSleepSummary.setText("Chưa có dữ liệu giấc ngủ 7 ngày gần nhất.");
            binding.tvSleepWarning.setVisibility(View.GONE);
            return;
        }

        double avg = SleepAnalyzer.averageDuration(logs);
        binding.tvSleepSummary.setText(String.format(Locale.getDefault(),
                "Trung bình 7 ngày: %s · %d bản ghi",
                SleepAnalyzer.formatDuration(avg),
                logs.size()));

        String warning = SleepAnalyzer.warningMessage(avg);
        if (warning != null) {
            binding.tvSleepWarning.setText(warning);
            binding.tvSleepWarning.setVisibility(View.VISIBLE);
        } else {
            binding.tvSleepWarning.setVisibility(View.GONE);
        }
    }

    // ─── Reminders ───────────────────────────────────────────────

    /**
     * Đặt nhắc uống nước mỗi 2 tiếng bắt đầu từ 8:00 sáng.
     * Dùng AlarmManager + repeating alarm.
     */
    private void scheduleWaterReminder() {
        AlarmManager alarmManager =
                (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(requireContext(), WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Bắt đầu lúc 8:00 sáng hôm nay (hoặc 8:00 ngày mai nếu đã qua)
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        if (startTime.getTimeInMillis() < System.currentTimeMillis()) {
            startTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Lặp mỗi 2 tiếng (7_200_000 ms)
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                startTime.getTimeInMillis(),
                2 * 60 * 60 * 1000L,
                pendingIntent);

        Toast.makeText(requireContext(),
                "Đã đặt nhắc uống nước mỗi 2 tiếng từ 8:00 sáng",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Đặt nhắc đi ngủ theo giờ người dùng đã chọn trong TimePicker.
     * Nếu chưa chọn, nhắc lúc 22:00 mặc định.
     */
    private void scheduleSleepReminder() {
        int bedHour   = 22;
        int bedMinute = 0;

        if (!selectedBedTime.isEmpty()) {
            try {
                String[] parts = selectedBedTime.split(":");
                bedHour   = Integer.parseInt(parts[0]);
                bedMinute = Integer.parseInt(parts[1]);
            } catch (Exception ignored) {}
        }

        AlarmManager alarmManager =
                (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Android 12+: kiểm tra quyền exact alarm trước
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            // Không có quyền → dùng setWindow thay thế (không exact nhưng không crash)
            Toast.makeText(requireContext(),
                    String.format(Locale.getDefault(),
                            "Đã đặt nhắc đi ngủ lúc %02d:%02d (gần đúng)", bedHour, bedMinute),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), SleepReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, bedHour);
        alarmTime.set(Calendar.MINUTE, bedMinute);
        alarmTime.set(Calendar.SECOND, 0);
        if (alarmTime.getTimeInMillis() < System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    pendingIntent);
        }

        Toast.makeText(requireContext(),
                String.format(Locale.getDefault(),
                        "Đã đặt nhắc đi ngủ lúc %02d:%02d", bedHour, bedMinute),
                Toast.LENGTH_SHORT).show();
    }

    // ─── Lifecycle ───────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
