package com.example.moblie_app.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moblie_app.MainActivity;
import com.example.moblie_app.databinding.FragmentSettingsBinding;
import com.example.moblie_app.utils.Constants;
import com.example.moblie_app.viewmodel.SettingsViewModel;

/**
 * SettingsFragment – Cài đặt ứng dụng:
 *  - Chuyển đổi Dark / Light mode
 *  - Đơn vị cân nặng (kg / lb)
 *  - Đơn vị khoảng cách (km / miles)
 *  - Xóa toàn bộ dữ liệu tài khoản
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, android.content.Context.MODE_PRIVATE);

        viewModel = new ViewModelProvider(
                this,
                new SettingsViewModel.Factory(requireActivity().getApplication()))
                .get(SettingsViewModel.class);

        loadCurrentSettings();
        setupListeners();
        observeViewModel();
    }

    // ─── Load saved settings ─────────────────────────────────────

    private void loadCurrentSettings() {
        // Dark mode
        boolean isDark = prefs.getBoolean(Constants.PREF_DARK_MODE, false);
        binding.switchDarkMode.setChecked(isDark);

        // Đơn vị cân nặng: "kg" (mặc định) hoặc "lb"
        String weightUnit = prefs.getString(Constants.PREF_UNIT_WEIGHT, "kg");
        binding.radioKg.setChecked("kg".equals(weightUnit));
        binding.radioLb.setChecked("lb".equals(weightUnit));

        // Đơn vị khoảng cách: "km" (mặc định) hoặc "miles"
        String distUnit = prefs.getString(Constants.PREF_UNIT_DISTANCE, "km");
        binding.radioKm.setChecked("km".equals(distUnit));
        binding.radioMiles.setChecked("miles".equals(distUnit));
    }

    // ─── Listeners ───────────────────────────────────────────────

    private void setupListeners() {
        // Nút Back
        binding.btnBack.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v).popBackStack());

        // Dark mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(Constants.PREF_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Đơn vị cân nặng
        binding.radioGroupWeight.setOnCheckedChangeListener((group, checkedId) -> {
            String unit = (checkedId == binding.radioKg.getId()) ? "kg" : "lb";
            prefs.edit().putString(Constants.PREF_UNIT_WEIGHT, unit).apply();
            Toast.makeText(requireContext(), "Đơn vị cân nặng: " + unit, Toast.LENGTH_SHORT).show();
        });

        // Đơn vị khoảng cách
        binding.radioGroupDistance.setOnCheckedChangeListener((group, checkedId) -> {
            String unit = (checkedId == binding.radioKm.getId()) ? "km" : "miles";
            prefs.edit().putString(Constants.PREF_UNIT_DISTANCE, unit).apply();
            Toast.makeText(requireContext(), "Đơn vị khoảng cách: " + unit, Toast.LENGTH_SHORT).show();
        });

        // Xóa dữ liệu tài khoản
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
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

        viewModel.getDeleteDone().observe(getViewLifecycleOwner(), done -> {
            if (Boolean.TRUE.equals(done)) {
                Toast.makeText(requireContext(),
                        "Đã xóa toàn bộ dữ liệu và đăng xuất.", Toast.LENGTH_LONG).show();
                navigateToLogin();
            }
        });
    }

    // ─── Delete account dialog ───────────────────────────────────

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa dữ liệu tài khoản")
                .setMessage("Toàn bộ dữ liệu sức khỏe của bạn (nhật ký ăn uống, vận động, giấc ngủ, nước uống) sẽ bị xóa vĩnh viễn và bạn sẽ bị đăng xuất.\n\nBạn có chắc chắn muốn tiếp tục?")
                .setPositiveButton("Xóa tất cả", (dialog, which) ->
                        viewModel.deleteAllUserData())
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finishAffinity();
    }

    // ─── Lifecycle ───────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
