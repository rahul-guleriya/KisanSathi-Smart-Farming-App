package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyFavorites;

    private ProductAdapter productAdapter;
    private final List<Product> favoriteProducts = new ArrayList<>();
    private DatabaseReference favRef;
    private ValueEventListener favEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rvFavorites);
        progressBar = findViewById(R.id.progressBarFavorites);
        layoutEmptyFavorites = findViewById(R.id.layoutEmptyFavorites);

        setupRecyclerView();
        loadFavoritesFromFirebase();
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(
                this,
                favoriteProducts,
                product -> {
                    Intent intent = new Intent(FavoritesActivity.this, ProductDetailsActivity.class);
                    intent.putExtra("PRODUCT_OBJ", product);
                    startActivity(intent);
                },
                (product, isCurrentlyFavorite) -> toggleFavorite(product)
        );
        rvFavorites.setAdapter(productAdapter);
    }

    private void toggleFavorite(Product product) {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null || product == null || product.getProductId() == null) return;

        FirebaseUtil.getFavoritesRef(userId).child(product.getProductId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(FavoritesActivity.this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FavoritesActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFavoritesFromFirebase() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, getString(R.string.login_required_fav), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        favRef = FirebaseUtil.getFavoritesRef(userId);

        favEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                progressBar.setVisibility(View.GONE);
                favoriteProducts.clear();
                Set<String> favIds = new HashSet<>();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Product product = childSnapshot.getValue(Product.class);
                    if (product != null) {
                        favoriteProducts.add(product);
                        if (product.getProductId() != null) {
                            favIds.add(product.getProductId());
                        }
                    }
                }

                productAdapter.setFavoriteProductIds(favIds);
                productAdapter.setProductList(favoriteProducts);

                if (favoriteProducts.isEmpty()) {
                    layoutEmptyFavorites.setVisibility(View.VISIBLE);
                    rvFavorites.setVisibility(View.GONE);
                } else {
                    layoutEmptyFavorites.setVisibility(View.GONE);
                    rvFavorites.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoritesActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            }
        };

        favRef.addValueEventListener(favEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (favRef != null && favEventListener != null) {
            favRef.removeEventListener(favEventListener);
        }
    }
}
