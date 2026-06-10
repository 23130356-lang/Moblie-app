package com.example.moblie_app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * DashboardFragment - TV1 sẽ hoàn thiện màn hình này.
 * Hiện tại chỉ là placeholder sau khi đăng nhập thành công.
 */
public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TextView tv = new TextView(requireContext());
        tv.setPadding(48, 96, 48, 48);
        tv.setTextSize(18f);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user != null ? user.getDisplayName() : "bạn";
        tv.setText("Chào mừng " + name + "!\n\nĐăng nhập thành công ✅\n\nDashboard đang được xây dựng...");
        return tv;
    }
}
