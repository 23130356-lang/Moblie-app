package com.example.moblie_app.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.moblie_app.R;
import com.example.moblie_app.databinding.FragmentRegisterBinding;
import com.example.moblie_app.utils.ValidationUtils;
import com.example.moblie_app.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        // Loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        // Lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(error);
            }
        });

        // Đăng ký thành công
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_register_to_dashboard);
            }
        });
    }

    private void setupClickListeners() {
        // Nút tạo tài khoản
        binding.btnRegister.setOnClickListener(v -> {
            String name            = binding.etName.getText().toString().trim();
            String email           = binding.etEmail.getText().toString().trim();
            String password        = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (!validate(name, email, password, confirmPassword)) return;

            binding.tvError.setVisibility(View.GONE);
            viewModel.register(email, password, name);
        });

        // Link quay lại đăng nhập
        binding.tvGoLogin.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_register_to_login));
    }

    private boolean validate(String name, String email,
                              String password, String confirmPassword) {
        // Kiểm tra tên
        if (!ValidationUtils.isNotEmpty(name)) {
            binding.tilName.setError("Vui lòng nhập họ và tên");
            return false;
        }
        binding.tilName.setError(null);

        // Kiểm tra email
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Email không hợp lệ");
            return false;
        }
        binding.tilEmail.setError(null);

        // Kiểm tra mật khẩu
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        binding.tilPassword.setError(null);

        // Kiểm tra xác nhận mật khẩu
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        }
        binding.tilConfirmPassword.setError(null);

        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
