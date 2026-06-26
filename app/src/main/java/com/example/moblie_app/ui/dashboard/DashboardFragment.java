package com.example.moblie_app.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.moblie_app.MainActivity;
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

        binding.tvSubtitle.setText("Chào bạn, " + name);
        binding.btnOpenFoodDiary.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_food_diary));
        binding.btnOpenProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_profile));
        binding.btnOpenGoals.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_goals));
        binding.btnOpenActivity.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_activity));
        binding.btnOpenCalorieChart.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_calorie_chart));
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finishAffinity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
