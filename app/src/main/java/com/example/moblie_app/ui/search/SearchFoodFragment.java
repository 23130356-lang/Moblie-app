package com.example.moblie_app.ui.search;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.databinding.FragmentSearchFoodBinding;
import com.example.moblie_app.databinding.ItemSearchProductBinding;
import com.example.moblie_app.model.MealEntryModel;
import com.example.moblie_app.model.MacroNutrients;
import com.example.moblie_app.network.of.NutrimentsModel;
import com.example.moblie_app.network.of.ProductModel;
import com.example.moblie_app.repository.FoodRepository;
import com.example.moblie_app.utils.Constants;
import com.example.moblie_app.utils.DateUtils;
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
    private ProductSearchAdapter adapter;
    private ProductModel selectedProduct;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String barcode = result.getContents();
                    binding.etSearch.setText(barcode);
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

        foodRepository = new FoodRepository();

        adapter = new ProductSearchAdapter(product -> {
            selectedProduct = product;
            showNutrition(product);
            showMealEntryDialog(product);
        });
        binding.rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResults.setAdapter(adapter);

        binding.btnAddToDiary.setOnClickListener(v -> {
            if (selectedProduct != null) {
                showMealEntryDialog(selectedProduct);
            }
        });

        binding.btnScanBarcode.setOnClickListener(v -> startBarcodeScan());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = binding.etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            binding.tvError.setText("Vui lòng nhập tên món ăn.");
            binding.tvError.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvError.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.GONE);
        binding.rvResults.setVisibility(View.GONE);
        binding.tvSectionResults.setVisibility(View.GONE);
        binding.cardNutrition.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        foodRepository.searchByName(query, new FoodRepository.SearchCallback() {
            @Override
            public void onSuccess(List<ProductModel> products, String source) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                adapter.submitList(products);
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
            binding.tvCalories.setText(formatValue(nutriments.getCalories()) + " kcal");
            binding.tvProtein.setText(formatValue(nutriments.getProtein()) + " g");
            binding.tvCarbs.setText(formatValue(nutriments.getCarbs()) + " g");
            binding.tvFat.setText(formatValue(nutriments.getFat()) + " g");
        } else {
            binding.tvCalories.setText("--");
            binding.tvProtein.setText("--");
            binding.tvCarbs.setText("--");
            binding.tvFat.setText("--");
        }
    }

    private void showMealEntryDialog(ProductModel product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(product.getProductName() != null
                ? product.getProductName() : "Thêm món ăn");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (getResources().getDisplayMetrics().density * 24);
        layout.setPadding(paddingPx, paddingPx / 2, paddingPx, 0);

        EditText quantityInput = new EditText(requireContext());
        quantityInput.setHint("Nhập số grams (mặc định 100)");
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        quantityInput.setText("100");
        layout.addView(quantityInput);

        RadioGroup radioGroup = new RadioGroup(requireContext());
        String[] mealLabels = {"Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ"};
        String[] mealKeys = {Constants.MEAL_BREAKFAST, Constants.MEAL_LUNCH,
                Constants.MEAL_DINNER, Constants.MEAL_SNACK};
        for (int i = 0; i < mealLabels.length; i++) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(mealLabels[i]);
            rb.setId(i);
            if (i == 3) rb.setChecked(true);
            radioGroup.addView(rb);
        }
        layout.addView(radioGroup);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String qtyStr = quantityInput.getText().toString().trim();
            double quantity;
            try {
                quantity = qtyStr.isEmpty() ? 100 : Double.parseDouble(qtyStr);
            } catch (NumberFormatException e) {
                showError("Khẩu phần không hợp lệ.");
                return;
            }
            if (quantity <= 0) {
                showError("Khẩu phần ăn phải lớn hơn 0 gram.");
                return;
            }
            int selectedId = radioGroup.getCheckedRadioButtonId();
            String mealType = mealKeys[selectedId >= 0 ? selectedId : 3];
            saveMealEntry(product, quantity, mealType);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveMealEntry(ProductModel product, double quantity, String mealType) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        NutrimentsModel n = product.getNutriments();
        double calsPer100 = n != null ? n.getCalories() : 0;
        double protPer100 = n != null ? n.getProtein() : 0;
        double carbsPer100 = n != null ? n.getCarbs() : 0;
        double fatPer100 = n != null ? n.getFat() : 0;

        MacroNutrients calculated = NutritionViewModel.calcNutrition(
                calsPer100, protPer100, carbsPer100, fatPer100, quantity
        );

        MealEntryModel meal = new MealEntryModel(
                null,
                product.getProductName() != null ? product.getProductName() : "Unknown",
                mealType,
                quantity,
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
                    Bundle result = new Bundle();
                    result.putBoolean("added", true);
                    Navigation.findNavController(requireView())
                            .getPreviousBackStackEntry()
                            .getSavedStateHandle()
                            .set("search_result", result);
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .addOnFailureListener(e -> showError("Không thể thêm: " + e.getMessage()));
    }

    private void showError(String msg) {
        binding.tvError.setText(msg);
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

    private static class ProductSearchAdapter
            extends RecyclerView.Adapter<ProductSearchAdapter.ViewHolder> {

        interface OnItemClickListener {
            void onItemClick(ProductModel product);
        }

        private final List<ProductModel> items = new ArrayList<>();
        private final OnItemClickListener listener;

        ProductSearchAdapter(OnItemClickListener listener) {
            this.listener = listener;
        }

        void submitList(List<ProductModel> products) {
            items.clear();
            if (products != null) {
                items.addAll(products);
            }
            notifyDataSetChanged();
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
                holder.binding.tvCalories.setText(formatValue(n.getCalories()) + " kcal");
                holder.binding.tvProtein.setText("P: " + formatValue(n.getProtein()) + "g");
                holder.binding.tvCarbs.setText("C: " + formatValue(n.getCarbs()) + "g");
                holder.binding.tvFat.setText("F: " + formatValue(n.getFat()) + "g");
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

            holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
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
}
