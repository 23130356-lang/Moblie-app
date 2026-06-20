package com.example.moblie_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;
import com.example.moblie_app.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Các màn hình KHÔNG hiện FAB "AI tư vấn sức khỏe":
    // - login/register (chưa đăng nhập)
    // - aiChatFragment (đang ở chính màn hình đó rồi)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        // Nếu đã đăng nhập -> set startDestination thẳng là dashboardFragment
        // (thay vì navigate chồng lên loginFragment, tránh loginFragment còn trong back stack)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            NavInflater inflater = navController.getNavInflater();
            NavGraph graph = inflater.inflate(R.navigation.nav_graph);
            graph.setStartDestination(R.id.dashboardFragment);
            navController.setGraph(graph);
        }

        setupAiChatFab(navController);
    }

    private void setupAiChatFab(NavController navController) {
        binding.fabAiChat.setOnClickListener(v ->
                navController.navigate(R.id.action_global_aiChatFragment));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean shouldShow = id != R.id.loginFragment
                    && id != R.id.registerFragment
                    && id != R.id.aiChatFragment;
            binding.fabAiChat.setVisibility(shouldShow ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }
}