package com.example.moblie_app.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.moblie_app.R;
import com.example.moblie_app.databinding.FragmentProfileBinding;
import com.example.moblie_app.model.UserModel;
import com.example.moblie_app.utils.AvatarHelper;
import com.example.moblie_app.utils.ValidationUtils;
import com.example.moblie_app.viewmodel.ProfileViewModel;

import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private String selectedAvatarKey = null;

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
        viewModel.loadProfile();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) showMessage(error, false);
        });

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) fillForm(user);
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) showMessage("Đã lưu hồ sơ thành công!", true);
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        binding.etDob.setOnClickListener(v -> showDatePicker());
        binding.tilDob.setEndIconOnClickListener(v -> showDatePicker());

        // Đổi giới tính → tự động cập nhật nhóm avatar
        binding.chipGroupGender.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_male)        onGenderChanged("male");
            else if (id == R.id.chip_female) onGenderChanged("female");
            else                             onGenderChanged("other");
        });

        // Avatar Nam
        binding.ivAvatarMaleThin.setOnClickListener(v   -> selectAvatar("male_thin"));
        binding.ivAvatarMaleNormal.setOnClickListener(v -> selectAvatar("male_normal"));
        binding.ivAvatarMaleFat.setOnClickListener(v    -> selectAvatar("male_fat"));

        // Avatar Nữ
        binding.ivAvatarFemaleThin.setOnClickListener(v   -> selectAvatar("female_thin"));
        binding.ivAvatarFemaleNormal.setOnClickListener(v -> selectAvatar("female_normal"));
        binding.ivAvatarFemaleFat.setOnClickListener(v    -> selectAvatar("female_fat"));

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void onGenderChanged(String gender) {
        if ("other".equals(gender)) {
            binding.layoutAvatarMale.setVisibility(View.GONE);
            binding.layoutAvatarFemale.setVisibility(View.GONE);
            binding.tvAvatarHint.setVisibility(View.VISIBLE);
            binding.tvAvatarHint.setText("Vui lòng chọn giới tính Nam hoặc Nữ để chọn avatar");
            binding.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            selectedAvatarKey = null;
        } else if ("male".equals(gender)) {
            binding.tvAvatarHint.setVisibility(View.GONE);
            binding.layoutAvatarMale.setVisibility(View.VISIBLE);
            binding.layoutAvatarFemale.setVisibility(View.GONE);
            if (!AvatarHelper.belongsToGender(selectedAvatarKey, "male")) {
                selectAvatar("male_normal");
            }
        } else {
            binding.tvAvatarHint.setVisibility(View.GONE);
            binding.layoutAvatarMale.setVisibility(View.GONE);
            binding.layoutAvatarFemale.setVisibility(View.VISIBLE);
            if (!AvatarHelper.belongsToGender(selectedAvatarKey, "female")) {
                selectAvatar("female_normal");
            }
        }
    }

    private void selectAvatar(String key) {
        selectedAvatarKey = key;
        binding.ivAvatar.setImageResource(AvatarHelper.getDrawableRes(key));
        resetAvatarBorders();
        ImageView selected = getAvatarViewByKey(key);
        if (selected != null) selected.setBackgroundResource(R.drawable.bg_avatar_selected);
    }

    private void resetAvatarBorders() {
        binding.ivAvatarMaleThin.setBackgroundResource(R.drawable.bg_avatar_unselected);
        binding.ivAvatarMaleNormal.setBackgroundResource(R.drawable.bg_avatar_unselected);
        binding.ivAvatarMaleFat.setBackgroundResource(R.drawable.bg_avatar_unselected);
        binding.ivAvatarFemaleThin.setBackgroundResource(R.drawable.bg_avatar_unselected);
        binding.ivAvatarFemaleNormal.setBackgroundResource(R.drawable.bg_avatar_unselected);
        binding.ivAvatarFemaleFat.setBackgroundResource(R.drawable.bg_avatar_unselected);
    }

    private ImageView getAvatarViewByKey(String key) {
        if (key == null) return null;
        switch (key) {
            case "male_thin":     return binding.ivAvatarMaleThin;
            case "male_normal":   return binding.ivAvatarMaleNormal;
            case "male_fat":      return binding.ivAvatarMaleFat;
            case "female_thin":   return binding.ivAvatarFemaleThin;
            case "female_normal": return binding.ivAvatarFemaleNormal;
            case "female_fat":    return binding.ivAvatarFemaleFat;
            default:              return null;
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, y, m, d) ->
                binding.etDob.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void fillForm(UserModel user) {
        binding.etName.setText(user.getFullName());
        binding.etEmail.setText(user.getEmail());
        binding.etDob.setText(user.getDateOfBirth());

        // Set giới tính → sẽ trigger onGenderChanged → hiện đúng nhóm avatar
        if ("male".equals(user.getGender()))        binding.chipMale.setChecked(true);
        else if ("female".equals(user.getGender())) binding.chipFemale.setChecked(true);
        else                                        binding.chipOther.setChecked(true);

        // Restore avatar đã lưu, hoặc mặc định theo giới tính
        String key = user.getAvatarKey() != null
                ? user.getAvatarKey()
                : AvatarHelper.getDefaultKey(user.getGender());
        if (key != null) selectAvatar(key);
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String dob  = binding.etDob.getText().toString().trim();

        if (!ValidationUtils.isNotEmpty(name)) {
            binding.tilName.setError("Vui lòng nhập họ và tên");
            return;
        }
        binding.tilName.setError(null);

        String gender = "other";
        int checkedId = binding.chipGroupGender.getCheckedChipId();
        if (checkedId == R.id.chip_male)        gender = "male";
        else if (checkedId == R.id.chip_female) gender = "female";

        UserModel current = viewModel.getUserProfile().getValue();
        if (current == null) current = new UserModel();
        current.setFullName(name);
        current.setDateOfBirth(dob);
        current.setGender(gender);
        current.setAvatarKey(selectedAvatarKey);

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