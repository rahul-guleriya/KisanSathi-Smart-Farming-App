package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductListActivity extends AppCompatActivity {

    private TextView tvCategoryHeaderTitle, tvEmptyProducts;
    private EditText etSearchProduct;
    private Spinner spinnerCategoryFilter;
    private RecyclerView rvProducts;
    private ProgressBar progressBar;

    private ProductAdapter productAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> displayedProducts = new ArrayList<>();
    private final Set<String> favoriteProductIds = new HashSet<>();
    private String selectedCategory = "All Categories";

    private DatabaseReference productsRef;
    private ValueEventListener productsEventListener;
    private DatabaseReference favoritesRef;
    private ValueEventListener favoritesEventListener;
    private boolean isSeeding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        tvCategoryHeaderTitle = findViewById(R.id.tvCategoryHeaderTitle);
        tvEmptyProducts = findViewById(R.id.tvEmptyProducts);
        etSearchProduct = findViewById(R.id.etSearchProduct);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);
        rvProducts = findViewById(R.id.rvProducts);
        progressBar = findViewById(R.id.progressBarProducts);

        if (getIntent() != null && getIntent().hasExtra("CATEGORY_NAME")) {
            String categoryExtra = getIntent().getStringExtra("CATEGORY_NAME");
            if (categoryExtra != null) {
                selectedCategory = categoryExtra;
            }
        }

        updateHeaderTitle();
        setupRecyclerView();
        setupCategorySpinner();
        setupSearchInput();
        loadFavoritesFromFirebase();
        loadProductsFromFirebase();
    }

    private void updateHeaderTitle() {
        String allCategoriesStr = getString(R.string.all_categories);
        if (selectedCategory.equalsIgnoreCase(allCategoriesStr) || "All Categories".equalsIgnoreCase(selectedCategory)) {
            tvCategoryHeaderTitle.setText(R.string.nav_categories);
        } else {
            tvCategoryHeaderTitle.setText(selectedCategory);
        }
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, displayedProducts,
                product -> {
                    Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
                    intent.putExtra("PRODUCT_OBJ", product);
                    startActivity(intent);
                },
                this::toggleFavorite
        );
        rvProducts.setAdapter(productAdapter);
    }

    private void setupCategorySpinner() {
        List<String> categories = Arrays.asList(
                getString(R.string.all_categories),
                getString(R.string.category_seeds),
                getString(R.string.category_fertilizers),
                getString(R.string.category_protection)
        );
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(spinnerAdapter);

        int initialPosition = categories.indexOf(selectedCategory);
        if (initialPosition >= 0) {
            spinnerCategoryFilter.setSelection(initialPosition);
        }

        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                updateHeaderTitle();
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSearchInput() {
        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                productAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadFavoritesFromFirebase() {
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
                    Toast.makeText(ProductListActivity.this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductListActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            favItemRef.setValue(product).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProductListActivity.this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductListActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadProductsFromFirebase() {
        if (allProducts.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        productsRef = FirebaseUtil.getProductsRef();
        productsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    if (!isSeeding) {
                        isSeeding = true;
                        populateSampleDatabaseRecords(productsRef);
                    }
                    return;
                }

                isSeeding = false;
                List<Product> temp = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Product product = itemSnapshot.getValue(Product.class);
                    if (product != null) {
                        temp.add(product);
                    }
                }

                allProducts.clear();
                allProducts.addAll(temp);
                filterProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductListActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            }
        };

        productsRef.addValueEventListener(productsEventListener);
    }

    private boolean isHindiLocale() {
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        if (!current.isEmpty() && current.get(0) != null && current.get(0).getLanguage() != null) {
            return "hi".equalsIgnoreCase(current.get(0).getLanguage());
        }
        return false;
    }

    private void filterProducts() {
        displayedProducts.clear();
        String allCategoriesStr = getString(R.string.all_categories);

        if (selectedCategory.equalsIgnoreCase(allCategoriesStr) || "All Categories".equalsIgnoreCase(selectedCategory)) {
            displayedProducts.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.getCategory() != null && (p.getCategory().equalsIgnoreCase(selectedCategory)
                        || matchCategoryAlias(p.getCategory(), selectedCategory))) {
                    displayedProducts.add(p);
                }
            }
        }

        productAdapter.setProductList(displayedProducts);

        if (displayedProducts.isEmpty()) {
            tvEmptyProducts.setVisibility(View.VISIBLE);
        } else {
            tvEmptyProducts.setVisibility(View.GONE);
        }
        rvProducts.setVisibility(View.VISIBLE);
    }

    private boolean matchCategoryAlias(String pCat, String selCat) {
        String seedsEn = "Seeds";
        String seedsHi = getString(R.string.category_seeds);
        if ((pCat.equalsIgnoreCase(seedsEn) || pCat.equalsIgnoreCase(seedsHi)) &&
                (selCat.equalsIgnoreCase(seedsEn) || selCat.equalsIgnoreCase(seedsHi))) {
            return true;
        }

        String fertEn = "Fertilizers";
        String fertHi = getString(R.string.category_fertilizers);
        if ((pCat.equalsIgnoreCase(fertEn) || pCat.equalsIgnoreCase(fertHi)) &&
                (selCat.equalsIgnoreCase(fertEn) || selCat.equalsIgnoreCase(fertHi))) {
            return true;
        }

        String protEn = "Crop Protection Products";
        String protEnShort = "Crop Protection";
        String protHi = getString(R.string.category_protection);
        return (pCat.equalsIgnoreCase(protEn) || pCat.equalsIgnoreCase(protEnShort) || pCat.equalsIgnoreCase(protHi)) &&
                (selCat.equalsIgnoreCase(protEn) || selCat.equalsIgnoreCase(protEnShort) || selCat.equalsIgnoreCase(protHi));
    }

    private void populateSampleDatabaseRecords(DatabaseReference productsRef) {
        Map<String, Object> batchUpdate = new HashMap<>();
        List<Product> sampleList = new ArrayList<>();
        boolean isHindi = isHindiLocale();

        String catSeeds = getString(R.string.category_seeds);
        String catFert = getString(R.string.category_fertilizers);
        String catProt = getString(R.string.category_protection);

        if (isHindi) {
            sampleList.add(new Product("p1", "एचडी-2967 हाइब्रिड गेहूं बीज", catSeeds, "राष्ट्रीय बीज निगम",
                    "10 किग्रा", 10.0, "किग्रा", 850.0, "2026-10-22",
                    "रबी मौसम में 20 सेमी की दूरी पर 40-45 किग्रा/एकड़ बोएं।",
                    "ठंडी और सूखी जगह पर रखें।", true));

            sampleList.add(new Product("p2", "पूसा बासमती 1121 धान बीज", catSeeds, "आईएआरआई बीज",
                    "5 किग्रा", 5.0, "किग्रा", 550.0, "2026-10-22",
                    "नर्सरी बुवाई से 24 घंटे पहले पानी में भिगोएं।",
                    "नमी से बचाकर रखें।", true));

            sampleList.add(new Product("p3", "पूसा बोल्ड सरसों बीज", catSeeds, "राजस्थान बीज",
                    "2 किग्रा", 2.0, "किग्रा", 320.0, "2026-10-22",
                    "1.5-2.0 किग्रा/एकड़ बीज दोमट मिट्टी में बोएं।",
                    "सूखे स्थान पर रखें।", true));

            sampleList.add(new Product("p4", "जेजी-14 चना बीज", catSeeds, "राज्य बीज निगम",
                    "8 किग्रा", 8.0, "किग्रा", 680.0, "2026-10-22",
                    "बुवाई से पहले राइजोबियम से बीजोपचार करें।",
                    "वायुरोधक स्थान पर रखें।", true));

            sampleList.add(new Product("p5", "एचक्यूपीएम-1 हाइब्रिड मक्का बीज", catSeeds, "एग्रीसीड्स इंडिया",
                    "4 किग्रा", 4.0, "किग्रा", 480.0, "2026-10-22",
                    "खरीफ या ज़ायद मौसम में 8 किग्रा/एकड़ बोएं।",
                    "ठंडे और सूखे स्थान में रखें।", true));

            sampleList.add(new Product("p6", "नीम कोटेड यूरिया (46% N)", catFert, "इफको",
                    "45 किग्रा", 45.0, "किग्रा", 266.5, "2026-10-22",
                    "पौधों की शुरुआती वानस्पतिक बढ़त के लिए नाइट्रोजन प्रदान करता है।",
                    "सूखी जगह पर रखें। बोरी को अच्छी तरह बंद रखें।", true));

            sampleList.add(new Product("p7", "डाई-अमोनियम फॉस्फेट (डीएपी 18-46-0)", catFert, "कृभको",
                    "50 किग्रा", 50.0, "किग्रा", 1350.0, "2026-10-22",
                    "मजबूत जड़ों के विकास के लिए फास्फोरस व नाइट्रोजन देता है।",
                    "नमी के संपर्क से बचाएं।", true));

            sampleList.add(new Product("p8", "एनपीके 19:19:19 खाद", catFert, "कोरोमंडल",
                    "25 किग्रा", 25.0, "किग्रा", 950.0, "2026-10-22",
                    "संतुलित पोषण (एन, पी, के) प्रदान करता है।",
                    "छायादार सूखी जगह पर रखें।", true));

            sampleList.add(new Product("p9", "जैविक बायो-कंपोस्ट खाद", catFert, "बायो-एग्रो",
                    "40 किग्रा", 40.0, "किग्रा", 400.0, "2026-10-22",
                    "मिट्टी की उपजाऊ क्षमता और जल धारण क्षमता बढ़ाता है।",
                    "हवादार सूखी जगह पर रखें।", true));

            sampleList.add(new Product("p10", "क्लोरापायरीफॉस 20% EC कीटनाशक", catProt, "क्रॉपकेयर इंडिया",
                    "1 लीटर", 1.0, "लीटर", 480.0, "2026-10-22",
                    "कीट नियंत्रण के लिए। 2 मिली प्रति लीटर पानी में मिलाकर छिड़काव करें।",
                    "दस्ताने और मास्क पहनें।", true));

            sampleList.add(new Product("p11", "मैनकोजेब 75% WP फफूंदनाशक", catProt, "प्लांटकेयर लिमिटेड",
                    "500 ग्राम", 0.5, "किग्रा", 320.0, "2026-10-22",
                    "फफूंद बीमारियों के नियंत्रण के लिए। 2.5 ग्राम प्रति लीटर पानी में मिलाएं।",
                    "उपयोग के बाद हाथ अच्छी तरह धोएं।", true));

            sampleList.add(new Product("p12", "ग्लाइफोसेट 41% SL खरपतवारनाशक", catProt, "एग्रीगार्ड",
                    "1 लीटर", 1.0, "लीटर", 520.0, "2026-10-22",
                    "बुवाई से पूर्व खरपतवार नियंत्रण के लिए उपयोग करें।",
                    "निर्देशों का ध्यानपूर्वक पालन करें।", true));
        } else {
            sampleList.add(new Product("p1", "HD-2967 Hybrid Wheat Seeds", catSeeds, "National Seeds Corp",
                    "10 kg", 10.0, "kg", 850.0, "2026-10-22",
                    "Sow 40-45 kg/acre during Rabi season with 20 cm row spacing.",
                    "Store in a cool, dry place.", true));

            sampleList.add(new Product("p2", "Pusa Basmati 1121 Rice Seeds", catSeeds, "IARI Seeds",
                    "5 kg", 5.0, "kg", 550.0, "2026-10-22",
                    "Soak seeds for 24 hours prior to nursery bed sowing.",
                    "Keep away from direct moisture before germination.", true));

            sampleList.add(new Product("p3", "Pusa Bold Mustard Seeds", catSeeds, "Rajasthan Seeds",
                    "2 kg", 2.0, "kg", 320.0, "2026-10-22",
                    "Sow 1.5-2.0 kg/acre in well-prepared loamy seedbed.",
                    "Protect from excessive moisture during storage.", true));

            sampleList.add(new Product("p4", "JG-14 Gram / Chana Seeds", catSeeds, "State Seed Corp",
                    "8 kg", 8.0, "kg", 680.0, "2026-10-22",
                    "Treat seeds with Rhizobium culture before sowing.",
                    "Store in dry airtight container.", true));

            sampleList.add(new Product("p5", "HQPM-1 Hybrid Maize Seeds", catSeeds, "AgriSeeds India",
                    "4 kg", 4.0, "kg", 480.0, "2026-10-22",
                    "Sow 8 kg/acre during Kharif or Summer season.",
                    "Store in cool and dry seed storage.", true));

            sampleList.add(new Product("p6", "Neem Coated Urea (46% N)", catFert, "IFFCO",
                    "45 kg", 45.0, "kg", 266.5, "2026-10-22",
                    "Provides Nitrogen for healthy vegetative leaf and plant growth.",
                    "Store in dry place. Keep bag closed tightly.", true));

            sampleList.add(new Product("p7", "Di-Ammonium Phosphate (DAP 18-46-0)", catFert, "KRIBHCO",
                    "50 kg", 50.0, "kg", 1350.0, "2026-10-22",
                    "Supplies Phosphorus & Nitrogen for strong root development.",
                    "Avoid contact with moisture before application.", true));

            sampleList.add(new Product("p8", "NPK 19:19:19 Complex Fertilizer", catFert, "Coromandel",
                    "25 kg", 25.0, "kg", 950.0, "2026-10-22",
                    "Balanced plant nutrients (N, P, K) for overall crop growth.",
                    "Keep bag sealed in shaded dry area.", true));

            sampleList.add(new Product("p9", "Organic Bio-Compost", catFert, "BioAgro Organic",
                    "40 kg", 40.0, "kg", 400.0, "2026-10-22",
                    "Improves soil fertility, structure and moisture retention.",
                    "Store in well-ventilated dry place.", true));

            sampleList.add(new Product("p10", "Chlorpyrifos 20% EC Insecticide", catProt, "CropCare India",
                    "1 L", 1.0, "L", 480.0, "2026-10-22",
                    "Dilute 2 ml per liter water as per approved label.",
                    "Wear protective gloves and mask.", true));

            sampleList.add(new Product("p11", "Mancozeb 75% WP Fungicide", catProt, "PlantCare Ltd",
                    "500 g", 0.5, "kg", 320.0, "2026-10-22",
                    "Fungicide control. Dissolve 2.5 g per liter water.",
                    "Wash hands thoroughly after spraying.", true));

            sampleList.add(new Product("p12", "Glyphosate 41% SL Weed Control", catProt, "AgriGuard",
                    "1 L", 1.0, "L", 520.0, "2026-10-22",
                    "Targeted application before land preparation.",
                    "Read product label carefully.", true));
        }

        for (Product p : sampleList) {
            batchUpdate.put(p.getProductId(), p);
        }

        productsRef.updateChildren(batchUpdate);
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
