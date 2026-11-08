package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kisansathi.models.Product;
import com.example.kisansathi.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvCategory, tvCompany, tvPackSize, tvPrice, tvUnitPrice, tvUpdatedDate, tvUsage, tvSafety;
    private MaterialButton btnFavorite, btnCompare;

    private Product product;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Bind Views
        tvName = findViewById(R.id.tvDetailProductName);
        tvCategory = findViewById(R.id.tvDetailCategoryBadge);
        tvCompany = findViewById(R.id.tvDetailCompany);
        tvPackSize = findViewById(R.id.tvDetailPackSize);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvUnitPrice = findViewById(R.id.tvDetailUnitPrice);
        tvUpdatedDate = findViewById(R.id.tvDetailUpdatedDate);
        tvUsage = findViewById(R.id.tvDetailUsage);
        tvSafety = findViewById(R.id.tvDetailSafety);

        btnFavorite = findViewById(R.id.btnToggleFavorite);
        btnCompare = findViewById(R.id.btnCompareThisProduct);

        // Get Product object
        if (getIntent() != null && getIntent().hasExtra("PRODUCT_OBJ")) {
            product = (Product) getIntent().getSerializableExtra("PRODUCT_OBJ");
        }

        if (product == null) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayProductDetails();
        checkFavoriteStatus();

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        btnCompare.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, PriceComparisonActivity.class);
            intent.putExtra("CATEGORY_NAME", product.getCategory());
            startActivity(intent);
        });
    }

    private void displayProductDetails() {
        tvName.setText(product.getName());
        tvCategory.setText(product.getCategory());
        tvCompany.setText(product.getCompany());
        tvPackSize.setText(product.getPackSize());

        tvPrice.setText(String.format(Locale.US, "₹%.2f", product.getPrice()));
        tvUnitPrice.setText(product.getFormattedUnitPrice());

        if (product.getUpdatedDate() != null) {
            tvUpdatedDate.setText(getString(R.string.last_updated, product.getUpdatedDate()));
        } else {
            tvUpdatedDate.setText(getString(R.string.last_updated, "N/A"));
        }

        if (product.getUsageInstructions() != null) {
            tvUsage.setText(product.getUsageInstructions());
        }

        if (product.getSafetyGuidelines() != null) {
            tvSafety.setText(product.getSafetyGuidelines());
        }
    }

    private void checkFavoriteStatus() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null || product.getProductId() == null) return;

        DatabaseReference favRef = FirebaseUtil.getFavoritesRef(userId).child(product.getProductId());
        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                isFavorite = snapshot.exists();
                updateFavoriteButtonUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void toggleFavorite() {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, getString(R.string.login_required_fav), Toast.LENGTH_SHORT).show();
            return;
        }

        if (product.getProductId() == null) return;

        DatabaseReference favRef = FirebaseUtil.getFavoritesRef(userId).child(product.getProductId());

        if (isFavorite) {
            favRef.removeValue().addOnCompleteListener(task -> {
                if (isFinishing() || isDestroyed()) return;
                if (task.isSuccessful()) {
                    isFavorite = false;
                    updateFavoriteButtonUI();
                    Toast.makeText(ProductDetailsActivity.this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailsActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            favRef.setValue(product).addOnCompleteListener(task -> {
                if (isFinishing() || isDestroyed()) return;
                if (task.isSuccessful()) {
                    isFavorite = true;
                    updateFavoriteButtonUI();
                    Toast.makeText(ProductDetailsActivity.this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailsActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateFavoriteButtonUI() {
        if (isFavorite) {
            btnFavorite.setText(getString(R.string.favorite_btn_remove));
            btnFavorite.setIconResource(R.drawable.ic_favorite);
        } else {
            btnFavorite.setText(getString(R.string.favorite_btn_add));
            btnFavorite.setIconResource(R.drawable.ic_favorite_border);
        }
    }
}
