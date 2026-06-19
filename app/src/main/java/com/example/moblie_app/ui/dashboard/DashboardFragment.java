package com.example.moblie_app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.moblie_app.R;
import com.example.moblie_app.databinding.FragmentDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * DashboardFragment - màn hình tổng quan sau đăng nhập.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

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

        binding.tvWelcome.setText("Chào mừng " + name);
        binding.btnOpenProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_profile));
        binding.btnOpenGoals.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_goals));
        binding.btnOpenActivity.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_activity));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
