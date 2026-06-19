package ui.diary;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.yourpackage.R;
import com.yourpackage.model.MealEntryModel;
import com.yourpackage.repository.NutritionRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DiaryFragment extends Fragment {

    private RecyclerView rvMeals;
    private MealAdapter adapter;

    private TabLayout tabLayout;

    private TextView txtDate;
    private TextView txtSummary;

    private Button btnPrevDay;
    private Button btnNextDay;

    private NutritionRepository repository;

    private Calendar selectedDate;

    private String currentMealType = "Breakfast";
    private static final String BREAKFAST = "Breakfast";
    private static final String LUNCH = "Lunch";
    private static final String DINNER = "Dinner";
    private static final String SNACK = "Snack";

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_diary,
                container,
                false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        repository = new NutritionRepository();

        selectedDate = Calendar.getInstance();

        initViews(view);

        setupRecyclerView();

        setupTabs();

        setupButtons();

        loadData();
    }

    private void initViews(View view){

        rvMeals = view.findViewById(R.id.rvMeals);

        tabLayout = view.findViewById(R.id.tabMeal);

        txtDate = view.findViewById(R.id.txtDate);

        txtSummary = view.findViewById(R.id.txtSummary);

        btnPrevDay = view.findViewById(R.id.btnPrevDay);

        btnNextDay = view.findViewById(R.id.btnNextDay);
    }

    private void setupRecyclerView(){

        adapter = new MealAdapter(meal -> {

            repository.deleteMeal(
                    meal.getDate(),
                    meal.getId(),
                    new NutritionRepository.FirestoreCallback<Boolean>() {

                        @Override
                        public void onSuccess(Boolean data) {

                            loadData();

                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });
        });

        rvMeals.setLayoutManager(
                new LinearLayoutManager(getContext()));

        rvMeals.setAdapter(adapter);
    }

    private void setupTabs(){

        tabLayout.addTab(
                tabLayout.newTab().setText("Sáng"));

        tabLayout.addTab(
                tabLayout.newTab().setText("Trưa"));

        tabLayout.addTab(
                tabLayout.newTab().setText("Tối"));

        tabLayout.addTab(
                tabLayout.newTab().setText("Phụ"));

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {

                        switch (tab.getPosition()){

                            case 0:
                                currentMealType = "Breakfast";
                                break;

                            case 1:
                                currentMealType = "Lunch";
                                break;

                            case 2:
                                currentMealType = "Dinner";
                                break;

                            case 3:
                                currentMealType = "Snack";
                                break;
                        }

                        loadData();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });
    }

    private void setupButtons(){

        btnPrevDay.setOnClickListener(v -> {

            selectedDate.add(Calendar.DAY_OF_MONTH,-1);

            loadData();
        });

        btnNextDay.setOnClickListener(v -> {

            selectedDate.add(Calendar.DAY_OF_MONTH,1);

            loadData();
        });
    }

    private void loadData(){

        String date = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault())
                .format(selectedDate.getTime());

        txtDate.setText(date);

        repository.getMealsByDate(
                date,
                new NutritionRepository.FirestoreCallback<List<MealEntryModel>>() {

                    @Override
                    public void onSuccess(List<MealEntryModel> meals) {

                        List<MealEntryModel> filtered =
                                new ArrayList<>();

                        double calories = 0;
                        double protein = 0;
                        double carbs = 0;
                        double fat = 0;

                        for(MealEntryModel meal : meals){

                            calories += meal.getCalories();
                            protein += meal.getProtein();
                            carbs += meal.getCarbs();
                            fat += meal.getFat();

                            if(currentMealType.equals(
                                    meal.getMealType())){

                                filtered.add(meal);
                            }
                        }

                        adapter.setData(filtered);

                        txtSummary.setText(
                                "Calories: " + calories +
                                        "\nProtein: " + protein +
                                        "\nCarbs: " + carbs +
                                        "\nFat: " + fat
                        );
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
    }
}