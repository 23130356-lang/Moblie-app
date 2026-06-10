package com.example.moblie_app.ui.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.moblie_app.R;
import com.example.moblie_app.databinding.FragmentProfileBinding;
import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.utils.ValidationUtils;
import com.example.moblie_app.viewmodel.ProfileViewModel;

import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    // Launcher chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getData() != null && result.getData().getData() != null) {
                            Uri imageUri = result.getData().getData();
                            // Hiện ảnh preview ngay lập tức
                            Glide.with(this).load(imageUri).circleCrop()
                                    .into(binding.ivAvatar);
                            // Upload lên Firebase Storage
                            viewModel.uploadAvatar(imageUri);
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        observeViewModel();
        setupClickListeners();

        // Tải hồ sơ khi mở màn hình
        viewModel.loadProfile();
    }

    private void observeViewModel() {
        // Loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!isLoading);
        });

        // Lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showMessage(error, false);
            }
        });

        // Dữ liệu hồ sơ
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) fillForm(user);
        });

        // Lưu thành công
        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                showMessage("Đã lưu hồ sơ thành công!", true);
            }
        });

        // Upload ảnh - progress
        viewModel.getUploadProgress().observe(getViewLifecycleOwner(), pct -> {
            if (pct != null && pct < 100) {
                binding.progressUpload.setVisibility(View.VISIBLE);
                binding.progressUpload.setProgress(pct);
            } else {
                binding.progressUpload.setVisibility(View.GONE);
            }
        });

        // Upload ảnh - hoàn thành
        viewModel.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).circleCrop().into(binding.ivAvatar);
                showMessage("Đã cập nhật ảnh đại diện!", true);
            }
        });
    }

    private void setupClickListeners() {
        // Nút back
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        // FAB chọn ảnh
        binding.fabChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // Chọn ngày sinh
        binding.etDob.setOnClickListener(v -> showDatePicker());
        binding.tilDob.setEndIconOnClickListener(v -> showDatePicker());

        // Nút lưu
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day   = cal.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (datePicker, y, m, d) -> {
            String date = String.format("%02d/%02d/%04d", d, m + 1, y);
            binding.etDob.setText(date);
        }, year, month, day).show();
    }

    private void fillForm(UserModel user) {
        binding.etName.setText(user.getFullName());
        binding.etEmail.setText(user.getEmail());
        binding.etDob.setText(user.getDateOfBirth());

        // Giới tính
        if ("male".equals(user.getGender())) {
            binding.chipMale.setChecked(true);
        } else if ("female".equals(user.getGender())) {
            binding.chipFemale.setChecked(true);
        } else if ("other".equals(user.getGender())) {
            binding.chipOther.setChecked(true);
        }

        // Ảnh đại diện
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);
        }
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String dob  = binding.etDob.getText().toString().trim();

        // Validate tên
        if (!ValidationUtils.isNotEmpty(name)) {
            binding.tilName.setError("Vui lòng nhập họ và tên");
            return;
        }
        binding.tilName.setError(null);

        // Lấy giới tính đã chọn
        String gender = "other";
        int checkedId = binding.chipGroupGender.getCheckedChipId();
        if (checkedId == R.id.chip_male)        gender = "male";
        else if (checkedId == R.id.chip_female) gender = "female";

        // Lấy user hiện tại và cập nhật
        UserModel current = viewModel.getUserProfile().getValue();
        if (current == null) current = new UserModel();

        current.setFullName(name);
        current.setDateOfBirth(dob);
        current.setGender(gender);

        viewModel.saveProfile(current);
    }

    private void showMessage(String msg, boolean isSuccess) {
        binding.tvMessage.setVisibility(View.VISIBLE);
        binding.tvMessage.setText(msg);
        binding.tvMessage.setTextColor(isSuccess
                ? requireContext().getColor(android.R.color.holo_green_dark)
                : requireContext().getColor(android.R.color.holo_red_dark));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
