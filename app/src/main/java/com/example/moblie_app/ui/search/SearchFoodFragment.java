package com.example.moblie_app.ui.search;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.databinding.FragmentSearchFoodBinding;
import com.example.moblie_app.databinding.ItemFavoriteBinding;
import com.example.moblie_app.databinding.ItemSearchProductBinding;
import com.example.moblie_app.model.FavoriteFoodModel;
import com.example.moblie_app.model.MealEntryModel;
import com.example.moblie_app.model.MacroNutrients;
import com.example.moblie_app.network.of.NutrimentsModel;
import com.example.moblie_app.network.of.ProductModel;
import com.example.moblie_app.repository.FoodRepository;
import com.example.moblie_app.utils.Constants;
import com.example.moblie_app.utils.DateUtils;
import com.example.moblie_app.utils.ServingUnit;
import com.example.moblie_app.viewmodel.FavoriteFoodViewModel;
import com.example.moblie_app.viewmodel.NutritionViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFoodFragment extends Fragment {

    private FragmentSearchFoodBinding binding;
    private FoodRepository foodRepository;
    private FavoriteFoodViewModel favViewModel;
    private ProductSearchAdapter productAdapter;
    private FavoriteAdapter favoriteAdapter;
    private ProductModel selectedProduct;
    private String preselectedMealType;

    private List<FavoriteFoodModel> favoriteList = new ArrayList<>();

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String barcode = result.getContents();
                    binding.etSearch.setText(barcode);
                    selectAllFoodsTab();
                    lookupBarcode(barcode);
                } else {
                    showError("Đã huỷ quét mã vạch.");
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchFoodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            preselectedMealType = getArguments().getString("mealType", "snack");
        } else {
            preselectedMealType = "snack";
        }

        foodRepository = new FoodRepository();
        favViewModel = new ViewModelProvider(
                this,
                new FavoriteFoodViewModel.Factory(requireContext()))
                .get(FavoriteFoodViewModel.class);

        setupProductAdapter();
        setupFavoriteAdapter();

        binding.rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResults.setAdapter(productAdapter);
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavorites.setAdapter(favoriteAdapter);

        binding.btnAddToDiary.setOnClickListener(v -> {
            if (selectedProduct != null) {
                showMealEntryDialog(selectedProduct.getProductName(),
                        selectedProduct.getServingSize(),
                        selectedProduct.getNutriments());
            }
        });

        binding.btnScanBarcode.setOnClickListener(v -> startBarcodeScan());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                int currentTab = binding.tabLayout.getSelectedTabPosition();
                if (currentTab == 0) {
                    filterFavoritesLocally();
                } else {
                    performSearch();
                }
                return true;
            }
            return false;
        });

        binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showFavoritesTab();
                } else {
                    showAllFoodsTab();
                }
            }
            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });

        favViewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            favoriteList = favorites != null ? favorites : new ArrayList<>();
            favoriteAdapter.submitList(favoriteList);
            boolean empty = favoriteList.isEmpty();
            binding.tvEmptyFavorites.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvFavorites.setVisibility(empty ? View.GONE : View.VISIBLE);

            List<String> names = new ArrayList<>();
            for (FavoriteFoodModel m : favoriteList) {
                if (m.getName() != null) names.add(m.getName().toLowerCase());
            }
            productAdapter.setFavoriteNames(names);
        });

        favViewModel.getActionDone().observe(getViewLifecycleOwner(), done -> {
            if (Boolean.TRUE.equals(done)) {
                favViewModel.onActionHandled();
            }
        });

        favViewModel.loadFavorites();
    }

    private void setupProductAdapter() {
        productAdapter = new ProductSearchAdapter(
                product -> {
                    selectedProduct = product;
                    showNutrition(product);
                },
                product -> favViewModel.toggleFavorite(product)
        );
    }

    private void setupFavoriteAdapter() {
        favoriteAdapter = new FavoriteAdapter(
                fav -> {
                    NutrimentsModel n = new NutrimentsModel(
                            fav.getCalories(), fav.getProtein(), fav.getCarbs(), fav.getFat());
                    showMealEntryDialog(fav.getName(), n);
                },
                fav -> favViewModel.removeFavorite(fav.getName())
        );
    }

    private void filterFavoritesLocally() {
        String query = binding.etSearch.getText().toString().trim().toLowerCase();
        List<FavoriteFoodModel> filtered = new ArrayList<>();
        for (FavoriteFoodModel fav : favoriteList) {
            if (query.isEmpty() || (fav.getName() != null && fav.getName().toLowerCase().contains(query))) {
                filtered.add(fav);
            }
        }
        favoriteAdapter.submitList(filtered);
        boolean empty = filtered.isEmpty();
        binding.tvEmptyFavorites.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvFavorites.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            if (query.isEmpty()) {
                binding.tvEmptyFavorites.setText("Chưa có món ăn yêu thích nào.\nHãy thêm ở tab Tất cả!");
            } else {
                binding.tvEmptyFavorites.setText("Không tìm thấy món yêu thích nào.");
            }
        }
    }

    private void selectAllFoodsTab() {
        com.google.android.material.tabs.TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
        if (tab != null) tab.select();
    }

    private void showFavoritesTab() {
        binding.layoutFavorites.setVisibility(View.VISIBLE);
        binding.layoutAllFoods.setVisibility(View.GONE);
    }

    private void showAllFoodsTab() {
        binding.layoutFavorites.setVisibility(View.GONE);
        binding.layoutAllFoods.setVisibility(View.VISIBLE);
    }

    private void performSearch() {
        String query = binding.etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            binding.tvError.setText("Vui lòng nhập tên món ăn.");
            binding.tvError.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvError.setVisibility(View.GONE);
        binding.rvResults.setVisibility(View.GONE);
        binding.tvSectionResults.setVisibility(View.GONE);
        binding.cardNutrition.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        foodRepository.searchByName(query, new FoodRepository.SearchCallback() {
            @Override
            public void onSuccess(List<ProductModel> products, String source) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                productAdapter.submitList(products);
                binding.rvResults.setVisibility(View.VISIBLE);
                binding.tvSectionResults.setVisibility(View.VISIBLE);
                binding.tvSectionResults.setText("Kết quả từ " + source);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setText(message);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startBarcodeScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt("Đưa mã vạch vào khung hình");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void lookupBarcode(String barcode) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvError.setVisibility(View.GONE);

        foodRepository.lookupByBarcode(barcode, new FoodRepository.BarcodeCallback() {
            @Override
            public void onSuccess(ProductModel product) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                selectedProduct = product;
                binding.etSearch.setText(product.getProductName());
                binding.rvResults.setVisibility(View.GONE);
                binding.tvSectionResults.setVisibility(View.GONE);
                showNutrition(product);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void showNutrition(ProductModel product) {
        binding.cardNutrition.setVisibility(View.VISIBLE);
        binding.tvProductName.setText(product.getProductName());

        String source = product.getSource();
        if (source != null) {
            binding.tvSourceLabel.setText("Nguồn: " + source);
            binding.tvSourceLabel.setVisibility(View.VISIBLE);
        } else {
            binding.tvSourceLabel.setVisibility(View.GONE);
        }

        NutrimentsModel nutriments = product.getNutriments();
        if (nutriments != null) {
            binding.tvCalories.setText(nutriments.getCalories() != null ? formatValue(nutriments.getCalories()) + " kcal" : "--");
            binding.tvProtein.setText(nutriments.getProtein() != null ? formatValue(nutriments.getProtein()) + " g" : "--");
            binding.tvCarbs.setText(nutriments.getCarbs() != null ? formatValue(nutriments.getCarbs()) + " g" : "--");
            binding.tvFat.setText(nutriments.getFat() != null ? formatValue(nutriments.getFat()) + " g" : "--");
        } else {
            binding.tvCalories.setText("--");
            binding.tvProtein.setText("--");
            binding.tvCarbs.setText("--");
            binding.tvFat.setText("--");
        }
    }

    private void showMealEntryDialog(String foodName, NutrimentsModel nutriments) {
        showMealEntryDialog(foodName, null, nutriments);
    }

    private void showMealEntryDialog(String foodName, String servingSize, NutrimentsModel nutriments) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(foodName != null ? foodName : "Thêm món ăn");

        int density = (int) (getResources().getDisplayMetrics().density);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = density * 24;
        layout.setPadding(paddingPx, paddingPx / 2, paddingPx, 0);

        List<ServingUnit> units;
        if (servingSize != null) {
            units = ServingUnit.parseOffServingSize(servingSize);
        } else {
            units = ServingUnit.getDefaults();
        }

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        EditText quantityInput = new EditText(requireContext());
        quantityInput.setHint("Số lượng");
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        quantityInput.setText("1");
        LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        quantityInput.setLayoutParams(qtyParams);
        row.addView(quantityInput);

        Spinner unitSpinner = new Spinner(requireContext());
        String[] unitLabels = ServingUnit.displayArray(units);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, unitLabels);
        unitSpinner.setAdapter(spinnerAdapter);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerParams.setMarginStart(density * 8);
        unitSpinner.setLayoutParams(spinnerParams);
        row.addView(unitSpinner);
        layout.addView(row);

        RadioGroup radioGroup = new RadioGroup(requireContext());
        String[] mealLabels = {"Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ"};
        String[] mealKeys = {Constants.MEAL_BREAKFAST, Constants.MEAL_LUNCH,
                Constants.MEAL_DINNER, Constants.MEAL_SNACK};
        int preSelectedIndex = 3;
        for (int i = 0; i < mealKeys.length; i++) {
            if (mealKeys[i].equals(preselectedMealType)) {
                preSelectedIndex = i;
                break;
            }
        }
        final int defaultIndex = preSelectedIndex;
        for (int i = 0; i < mealLabels.length; i++) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(mealLabels[i]);
            rb.setId(i);
            if (i == defaultIndex) rb.setChecked(true);
            radioGroup.addView(rb);
        }
        layout.addView(radioGroup);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String qtyStr = quantityInput.getText().toString().trim();
            double unitQty;
            try {
                unitQty = qtyStr.isEmpty() ? 1 : Double.parseDouble(qtyStr.replace(",", "."));
            } catch (NumberFormatException e) {
                showError("Khẩu phần không hợp lệ.");
                return;
            }
            if (unitQty <= 0) {
                showError("Khẩu phần ăn phải lớn hơn 0.");
                return;
            }

            int unitPos = unitSpinner.getSelectedItemPosition();
            ServingUnit selectedUnit = units.get(unitPos);
            double grams = unitQty * selectedUnit.getGrams();

            int selectedId = radioGroup.getCheckedRadioButtonId();
            String mealType = mealKeys[selectedId >= 0 ? selectedId : defaultIndex];
            saveMealEntry(foodName, nutriments, grams, selectedUnit.getLabel(), unitQty, mealType);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveMealEntry(String foodName, NutrimentsModel nutriments,
                                double grams, String unitLabel, double unitQty, String mealType) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        double calsPer100 = nutriments != null && nutriments.getCalories() != null ? nutriments.getCalories() : 0;
        double protPer100 = nutriments != null && nutriments.getProtein() != null ? nutriments.getProtein() : 0;
        double carbsPer100 = nutriments != null && nutriments.getCarbs() != null ? nutriments.getCarbs() : 0;
        double fatPer100 = nutriments != null && nutriments.getFat() != null ? nutriments.getFat() : 0;

        MacroNutrients calculated = NutritionViewModel.calcNutrition(
                calsPer100, protPer100, carbsPer100, fatPer100, grams
        );

        MealEntryModel meal = new MealEntryModel(
                null,
                foodName != null ? foodName : "Unknown",
                mealType,
                grams,
                unitLabel != null ? unitLabel : "g",
                unitQty,
                calculated.getCalories(),
                calculated.getProtein(),
                calculated.getCarbs(),
                calculated.getFat(),
                DateUtils.now(),
                DateUtils.getTodayKey()
        );

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection("meals")
                .document(DateUtils.getTodayKey())
                .collection("entries")
                .add(meal)
                .addOnSuccessListener(documentReference -> {
                    binding.tvError.setText("Đã thêm vào nhật ký!");
                    binding.tvError.setTextColor(androidx.core.content.ContextCompat.getColor(
                            requireContext(), com.example.moblie_app.R.color.health_success));
                    binding.tvError.setVisibility(View.VISIBLE);
                    selectedProduct = null;
                    binding.cardNutrition.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> showError("Không thể thêm: " + e.getMessage()));
    }

    private void showError(String msg) {
        binding.tvError.setText(msg);
        binding.tvError.setTextColor(androidx.core.content.ContextCompat.getColor(
                requireContext(), com.example.moblie_app.R.color.health_error));
        binding.tvError.setVisibility(View.VISIBLE);
    }

    public static String formatValue(double value) {
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ============================================================
    // ProductSearchAdapter
    // ============================================================
    private static class ProductSearchAdapter
            extends RecyclerView.Adapter<ProductSearchAdapter.ViewHolder> {

        interface OnItemClickListener {
            void onItemClick(ProductModel product);
        }

        interface OnFavClickListener {
            void onFavClick(ProductModel product);
        }

        private final List<ProductModel> items = new ArrayList<>();
        private final OnItemClickListener itemListener;
        private final OnFavClickListener favListener;
        private List<String> favoriteNames = new ArrayList<>();

        ProductSearchAdapter(OnItemClickListener itemListener,
                             OnFavClickListener favListener) {
            this.itemListener = itemListener;
            this.favListener = favListener;
        }

        void submitList(List<ProductModel> products) {
            items.clear();
            if (products != null) {
                items.addAll(products);
            }
            notifyDataSetChanged();
        }

        void setFavoriteNames(List<String> names) {
            this.favoriteNames = names != null ? names : new ArrayList<>();
            notifyDataSetChanged();
        }

        private boolean isFavorite(String name) {
            if (name == null) return false;
            return favoriteNames.contains(name.toLowerCase());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSearchProductBinding b = ItemSearchProductBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProductModel product = items.get(position);
            holder.binding.tvProductName.setText(product.getProductName() != null
                    ? product.getProductName() : "(không có tên)");

            NutrimentsModel n = product.getNutriments();
            if (n != null) {
                holder.binding.tvCalories.setText(n.getCalories() != null ? formatValue(n.getCalories()) + " kcal" : "--");
                holder.binding.tvProtein.setText("P: " + (n.getProtein() != null ? formatValue(n.getProtein()) : "--") + "g");
                holder.binding.tvCarbs.setText("C: " + (n.getCarbs() != null ? formatValue(n.getCarbs()) : "--") + "g");
                holder.binding.tvFat.setText("F: " + (n.getFat() != null ? formatValue(n.getFat()) : "--") + "g");
            } else {
                holder.binding.tvCalories.setText("--");
                holder.binding.tvProtein.setText("--");
                holder.binding.tvCarbs.setText("--");
                holder.binding.tvFat.setText("--");
            }

            String source = product.getSource();
            if (source != null) {
                holder.binding.tvSource.setText(source);
                holder.binding.tvSource.setVisibility(View.VISIBLE);
            } else {
                holder.binding.tvSource.setVisibility(View.GONE);
            }

            boolean fav = isFavorite(product.getProductName());
            android.content.res.ColorStateList tint;
            if (fav) {
                tint = android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(
                                holder.itemView.getContext(), com.example.moblie_app.R.color.health_mint));
            } else {
                tint = android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(
                                holder.itemView.getContext(), com.example.moblie_app.R.color.health_text_hint));
            }
            holder.binding.btnFavorite.setIconResource(fav
                    ? com.example.moblie_app.R.drawable.ic_heart_filled
                    : com.example.moblie_app.R.drawable.ic_heart_outline);
            holder.binding.btnFavorite.setIconTint(tint);

            holder.itemView.setOnClickListener(v -> itemListener.onItemClick(product));
            holder.binding.btnFavorite.setOnClickListener(v -> favListener.onFavClick(product));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemSearchProductBinding binding;

            ViewHolder(ItemSearchProductBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    // ============================================================
    // FavoriteAdapter
    // ============================================================
    private static class FavoriteAdapter
            extends RecyclerView.Adapter<FavoriteAdapter.FavViewHolder> {

        interface OnFavItemClickListener {
            void onItemClick(FavoriteFoodModel fav);
        }

        interface OnFavHeartClickListener {
            void onHeartClick(FavoriteFoodModel fav);
        }

        private final List<FavoriteFoodModel> items = new ArrayList<>();
        private final OnFavItemClickListener addListener;
        private final OnFavHeartClickListener heartListener;

        FavoriteAdapter(OnFavItemClickListener addListener,
                        OnFavHeartClickListener heartListener) {
            this.addListener = addListener;
            this.heartListener = heartListener;
        }

        void submitList(List<FavoriteFoodModel> list) {
            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFavoriteBinding b = ItemFavoriteBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new FavViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
            FavoriteFoodModel fav = items.get(position);
            holder.binding.tvProductName.setText(fav.getName() != null ? fav.getName() : "(không có tên)");

            double cals = fav.getCalories();
            double prot = fav.getProtein();
            double carbs = fav.getCarbs();
            double fat = fav.getFat();

            boolean hasCals = cals > 0 || (cals == 0 && fav.getCalories() != 0);
            boolean hasProt = prot > 0 || (prot == 0 && fav.getProtein() != 0);
            boolean hasCarbs = carbs > 0 || (carbs == 0 && fav.getCarbs() != 0);
            boolean hasFat = fat > 0 || (fat == 0 && fav.getFat() != 0);

            if (hasCals) {
                holder.binding.tvCalories.setText(formatValue(cals) + " kcal");
                holder.binding.tvCalories.setVisibility(View.VISIBLE);
            } else {
                holder.binding.tvCalories.setVisibility(View.GONE);
            }

            holder.binding.tvProtein.setText("P: " + (hasProt ? formatValue(prot) : "--") + "g");
            holder.binding.tvCarbs.setText("C: " + (hasCarbs ? formatValue(carbs) : "--") + "g");
            holder.binding.tvFat.setText("F: " + (hasFat ? formatValue(fat) : "--") + "g");

            String source = fav.getSource();
            if (source != null && !source.isEmpty()) {
                holder.binding.tvSource.setText(source);
                holder.binding.tvSource.setVisibility(View.VISIBLE);
            } else {
                holder.binding.tvSource.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> addListener.onItemClick(fav));
            holder.binding.btnFavorite.setOnClickListener(v -> heartListener.onHeartClick(fav));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class FavViewHolder extends RecyclerView.ViewHolder {
            final ItemFavoriteBinding binding;

            FavViewHolder(ItemFavoriteBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
