package com.example.moblie_app.ui.auth;

import android.content.Intent;
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
import com.example.moblie_app.databinding.FragmentLoginBinding;
import com.example.moblie_app.utils.ValidationUtils;
import com.example.moblie_app.viewmodel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginFragment extends Fragment {

    private static final int RC_SIGN_IN = 100;

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    // Guard tránh navigate nhiều lần khi LiveData re-emit
    private boolean hasNavigated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Reset guard mỗi lần Fragment được tạo lại
        hasNavigated = false;

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupGoogleSignIn();
        observeViewModel();
        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
            binding.btnGoogle.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(error);
            }
        });

        // Guard hasNavigated: tránh navigate lại khi LiveData re-emit do back stack
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && !hasNavigated) {
                hasNavigated = true;
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_dashboard);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email    = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (!validate(email, password)) return;

            binding.tvError.setVisibility(View.GONE);
            viewModel.login(email, password);
        });

        binding.btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        binding.tvGoRegister.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_register));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                viewModel.loginWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText("Đăng nhập Google thất bại: " + e.getMessage());
            }
        }
    }

    private boolean validate(String email, String password) {
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Email không hợp lệ");
            return false;
        }
        binding.tilEmail.setError(null);

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        binding.tilPassword.setError(null);
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}