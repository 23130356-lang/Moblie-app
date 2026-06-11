package com.example.moblie_app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.moblie_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 96, 48, 48);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user != null && user.getDisplayName() != null
                ? user.getDisplayName() : "bạn";

        TextView tv = new TextView(requireContext());
        tv.setTextSize(18f);
        tv.setText("Chào mừng " + name + "!\n\nDashboard đang được xây dựng...");
        layout.addView(tv);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 48;

        // Nút mở màn hình Profile
        Button btnProfile = new Button(requireContext());
        btnProfile.setText("Xem hồ sơ cá nhân");
        btnProfile.setLayoutParams(params);
        btnProfile.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_to_profile));
        layout.addView(btnProfile);

        // Nút mở màn hình Mục tiêu sức khỏe
        Button btnGoals = new Button(requireContext());
        btnGoals.setText("🎯  Thiết lập mục tiêu sức khỏe");
        btnGoals.setLayoutParams(params);
        btnGoals.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_to_goals));
        layout.addView(btnGoals);

        return layout;
    }
}