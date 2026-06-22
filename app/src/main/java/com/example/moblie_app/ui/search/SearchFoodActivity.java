package com.example.moblie_app.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.databinding.ActivitySearchFoodBinding;
import com.example.moblie_app.network.of.NutrimentsModel;
import com.example.moblie_app.network.of.ProductModel;
import com.example.moblie_app.repository.FoodRepository;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFoodActivity extends AppCompatActivity {

    private ActivitySearchFoodBinding binding;
    private FoodRepository foodRepository;
    private ProductAdapter adapter;
    private ProductModel selectedProduct;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        foodRepository = new FoodRepository();

        adapter = new ProductAdapter(product -> {
            selectedProduct = product;
            showNutrition(product);
        });
        binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResults.setAdapter(adapter);

        binding.btnSearch.setOnClickListener(v -> performSearch());
        binding.btnScanBarcode.setOnClickListener(v -> startBarcodeScan());
        binding.btnAddToDiary.setOnClickListener(v -> addToDiary());

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
                binding.progressBar.setVisibility(View.GONE);
                adapter.submitList(products);
                binding.rvResults.setVisibility(View.VISIBLE);
                binding.tvSectionResults.setVisibility(View.VISIBLE);
                binding.tvSectionResults.setText("Kết quả từ " + source);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setText(message);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startBarcodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Đưa mã vạch vào khung hình");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                showError("Đã huỷ quét mã vạch.");
            } else {
                lookupBarcode(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void lookupBarcode(String barcode) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvError.setVisibility(View.GONE);

        foodRepository.lookupByBarcode(barcode, new FoodRepository.BarcodeCallback() {
            @Override
            public void onSuccess(ProductModel product) {
                binding.progressBar.setVisibility(View.GONE);
                selectedProduct = product;
                binding.etSearch.setText(product.getProductName());
                binding.rvResults.setVisibility(View.GONE);
                binding.tvSectionResults.setVisibility(View.GONE);
                showNutrition(product);
            }

            @Override
            public void onError(String message) {
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

    private void addToDiary() {
        if (selectedProduct == null) return;
        Intent data = new Intent();
        data.putExtra("product_name", selectedProduct.getProductName());
        data.putExtra("image_url", selectedProduct.getImageUrl());
        NutrimentsModel n = selectedProduct.getNutriments();
        if (n != null) {
            data.putExtra("calories", n.getCalories());
            data.putExtra("protein", n.getProtein());
            data.putExtra("carbs", n.getCarbs());
            data.putExtra("fat", n.getFat());
        }
        setResult(RESULT_OK, data);
        finish();
    }

    private void showError(String msg) {
        binding.tvError.setText(msg);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private String formatValue(double value) {
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private static class ProductAdapter
            extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

        interface OnItemClickListener {
            void onItemClick(ProductModel product);
        }

        private final List<ProductModel> items = new ArrayList<>();
        private final OnItemClickListener listener;

        ProductAdapter(OnItemClickListener listener) {
            this.listener = listener;
        }

        void submitList(List<ProductModel> products) {
            items.clear();
            if (products != null) {
                items.addAll(products);
            }
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ProductModel product = items.get(position);
            holder.text1.setText(product.getProductName() != null
                    ? product.getProductName() : "(không có tên)");

            String sourceText = "";
            if (product.getSource() != null) {
                sourceText = " [" + product.getSource() + "]";
            }

            NutrimentsModel n = product.getNutriments();
            if (n != null) {
                holder.text2.setText(String.format(Locale.getDefault(),
                        "Calo: %s kcal · P: %s g · C: %s g · F: %s g%s",
                        formatValueStatic(n.getCalories()),
                        formatValueStatic(n.getProtein()),
                        formatValueStatic(n.getCarbs()),
                        formatValueStatic(n.getFat()),
                        sourceText));
            } else {
                holder.text2.setText("Không có thông tin dinh dưỡng" + sourceText);
            }
            holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private static String formatValueStatic(double value) {
            if (value == (long) value) {
                return String.format(Locale.US, "%d", (long) value);
            }
            return String.format(Locale.US, "%.1f", value);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text1;
            final TextView text2;

            ViewHolder(android.view.View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
