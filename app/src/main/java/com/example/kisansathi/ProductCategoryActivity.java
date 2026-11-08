package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class ProductCategoryActivity extends AppCompatActivity {

    private MaterialCardView cardSeeds, cardFertilizers, cardProtection, cardComparison;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_category);

        cardSeeds = findViewById(R.id.cardCategorySeeds);
        cardFertilizers = findViewById(R.id.cardCategoryFertilizers);
        cardProtection = findViewById(R.id.cardCategoryProtection);
        cardComparison = findViewById(R.id.cardOpenComparison);

        cardSeeds.setOnClickListener(v -> openProductList("Seeds"));
        cardFertilizers.setOnClickListener(v -> openProductList("Fertilizers"));
        cardProtection.setOnClickListener(v -> openProductList("Crop Protection Products"));

        cardComparison.setOnClickListener(v -> {
            Intent intent = new Intent(ProductCategoryActivity.this, PriceComparisonActivity.class);
            startActivity(intent);
        });
    }

    private void openProductList(String categoryName) {
        Intent intent = new Intent(ProductCategoryActivity.this, ProductListActivity.class);
        intent.putExtra("CATEGORY_NAME", categoryName);
        startActivity(intent);
    }
}
