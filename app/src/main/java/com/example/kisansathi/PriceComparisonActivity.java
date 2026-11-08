package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kisansathi.adapters.ProductAdapter;
import com.example.kisansathi.models.Product;
import com.example.kisansathi.utils.FirebaseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PriceComparisonActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private RecyclerView rvComparison;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private ProductAdapter productAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> comparedProducts = new ArrayList<>();
    private final Set<String> favoriteProductIds = new HashSet<>();
    private String selectedCategory = "All Categories";

    private DatabaseReference productsRef;
    private ValueEventListener productsEventListener;
    private DatabaseReference favoritesRef;
    private ValueEventListener favoritesEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_comparison);

        spinnerCategory = findViewById(R.id.spinnerCompareCategory);
        rvComparison = findViewById(R.id.rvComparisonList);
        progressBar = findViewById(R.id.progressBarCompare);
        tvEmpty = findViewById(R.id.tvEmptyComparison);

        if (getIntent() != null && getIntent().hasExtra("CATEGORY_NAME")) {
            String categoryExtra = getIntent().getStringExtra("CATEGORY_NAME");
            if (categoryExtra != null) {
                selectedCategory = categoryExtra;
            }
        }

        setupRecyclerView();
        setupCategorySpinner();
        loadFavoritesFromDatabase();
        loadProductsFromDatabase();
    }

    private void setupRecyclerView() {
        rvComparison.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(
                this,
                comparedProducts,
                product -> {
                    Intent intent = new Intent(PriceComparisonActivity.this, ProductDetailsActivity.class);
                    intent.putExtra("PRODUCT_OBJ", product);
                    startActivity(intent);
                },
                this::toggleFavorite
        );
        rvComparison.setAdapter(productAdapter);
    }

    private void setupCategorySpinner() {
        List<String> categories = Arrays.asList("All Categories", "Seeds", "Fertilizers", "Crop Protection Products");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        int initialPos = categories.indexOf(selectedCategory);
        if (initialPos >= 0) {
            spinnerCategory.setSelection(initialPos);
        }

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                filterAndSortProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadFavoritesFromDatabase() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) return;

        favoritesRef = FirebaseUtil.getFavoritesRef(userId);
        favoritesEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                favoriteProductIds.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String pId = child.getKey();
                        if (pId != null) {
                            favoriteProductIds.add(pId);
                        }
                    }
                }
                productAdapter.setFavoriteProductIds(favoriteProductIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        favoritesRef.addValueEventListener(favoritesEventListener);
    }

    private void toggleFavorite(Product product, boolean isCurrentlyFavorite) {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, getString(R.string.login_required_fav), Toast.LENGTH_SHORT).show();
            return;
        }

        if (product == null || product.getProductId() == null) return;

        DatabaseReference favItemRef = FirebaseUtil.getFavoritesRef(userId).child(product.getProductId());

        if (isCurrentlyFavorite) {
            favItemRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(PriceComparisonActivity.this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PriceComparisonActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            favItemRef.setValue(product).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(PriceComparisonActivity.this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PriceComparisonActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadProductsFromDatabase() {
        progressBar.setVisibility(View.VISIBLE);
        productsRef = FirebaseUtil.getProductsRef();

        productsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                allProducts.clear();

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Product p = itemSnapshot.getValue(Product.class);
                    if (p != null) {
                        allProducts.add(p);
                    }
                }

                filterAndSortProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PriceComparisonActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            }
        };

        productsRef.addValueEventListener(productsEventListener);
    }

    private void filterAndSortProducts() {
        comparedProducts.clear();

        if (selectedCategory.equals("All Categories")) {
            comparedProducts.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(selectedCategory)) {
                    comparedProducts.add(p);
                }
            }
        }

        // Sort products by unit price ascending (Lowest unit rate first)
        Collections.sort(comparedProducts, Comparator.comparingDouble(Product::getUnitPrice));

        productAdapter.setProductList(comparedProducts);

        if (comparedProducts.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvComparison.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvComparison.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (productsRef != null && productsEventListener != null) {
            productsRef.removeEventListener(productsEventListener);
        }
        if (favoritesRef != null && favoritesEventListener != null) {
            favoritesRef.removeEventListener(favoritesEventListener);
        }
    }
}
