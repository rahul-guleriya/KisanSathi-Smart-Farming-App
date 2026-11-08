package com.example.kisansathi;

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

import com.example.kisansathi.adapters.MandiAdapter;
import com.example.kisansathi.models.MandiPrice;
import com.example.kisansathi.utils.FirebaseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MandiPriceActivity extends AppCompatActivity {

    private EditText etSearchMandi;
    private Spinner spinnerStateFilter;
    private RecyclerView rvMandiPrices;
    private ProgressBar progressBar;
    private TextView tvEmptyMandi;

    private MandiAdapter mandiAdapter;
    private final List<MandiPrice> allMandiPrices = new ArrayList<>();
    private final List<MandiPrice> displayedMandiPrices = new ArrayList<>();

    private String currentSearchQuery = "";
    private String selectedState = "All States";

    private DatabaseReference mandiRef;
    private ValueEventListener mandiEventListener;
    private boolean isSeeding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi_price);

        etSearchMandi = findViewById(R.id.etSearchMandi);
        spinnerStateFilter = findViewById(R.id.spinnerStateFilter);
        rvMandiPrices = findViewById(R.id.rvMandiPrices);
        progressBar = findViewById(R.id.progressBarMandi);
        tvEmptyMandi = findViewById(R.id.tvEmptyMandi);

        setupRecyclerView();
        setupStateSpinner();
        setupSearchInput();
        loadMandiPricesFromFirebase();
    }

    private void setupRecyclerView() {
        rvMandiPrices.setLayoutManager(new LinearLayoutManager(this));
        mandiAdapter = new MandiAdapter(this, displayedMandiPrices);
        rvMandiPrices.setAdapter(mandiAdapter);
    }

    private void setupStateSpinner() {
        List<String> states = Arrays.asList(
                getString(R.string.all_states),
                "Punjab", "Haryana", "Uttar Pradesh", "Madhya Pradesh", "Rajasthan", "Maharashtra"
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, states);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStateFilter.setAdapter(adapter);

        spinnerStateFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = states.get(position);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSearchInput() {
        etSearchMandi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadMandiPricesFromFirebase() {
        if (allMandiPrices.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        mandiRef = FirebaseUtil.getMandiPricesRef();
        mandiEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    if (!isSeeding) {
                        isSeeding = true;
                        populateSampleMandiRecords(mandiRef);
                    }
                    return;
                }

                isSeeding = false;
                List<MandiPrice> temp = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    MandiPrice item = itemSnapshot.getValue(MandiPrice.class);
                    if (item != null) {
                        temp.add(item);
                    }
                }

                allMandiPrices.clear();
                allMandiPrices.addAll(temp);
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MandiPriceActivity.this, "Failed to load mandi prices: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mandiRef.addValueEventListener(mandiEventListener);
    }

    private boolean isHindiLocale() {
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        if (!current.isEmpty() && current.get(0) != null && current.get(0).getLanguage() != null) {
            return "hi".equalsIgnoreCase(current.get(0).getLanguage());
        }
        return false;
    }

    private void applyFilters() {
        displayedMandiPrices.clear();
        String queryPattern = currentSearchQuery.toLowerCase(Locale.US).trim();
        String allStatesStr = getString(R.string.all_states);

        for (MandiPrice item : allMandiPrices) {
            boolean matchState = selectedState.equalsIgnoreCase(allStatesStr) ||
                    "All States".equalsIgnoreCase(selectedState) ||
                    (item.getState() != null && item.getState().equalsIgnoreCase(selectedState));

            boolean matchSearch = queryPattern.isEmpty() ||
                    (item.getCropName() != null && item.getCropName().toLowerCase(Locale.US).contains(queryPattern)) ||
                    (item.getMandiName() != null && item.getMandiName().toLowerCase(Locale.US).contains(queryPattern)) ||
                    (item.getState() != null && item.getState().toLowerCase(Locale.US).contains(queryPattern)) ||
                    (item.getDistrict() != null && item.getDistrict().toLowerCase(Locale.US).contains(queryPattern));

            if (matchState && matchSearch) {
                displayedMandiPrices.add(item);
            }
        }

        mandiAdapter.updateList(displayedMandiPrices);

        if (displayedMandiPrices.isEmpty()) {
            tvEmptyMandi.setVisibility(View.VISIBLE);
        } else {
            tvEmptyMandi.setVisibility(View.GONE);
        }
        rvMandiPrices.setVisibility(View.VISIBLE);
    }

    private void populateSampleMandiRecords(DatabaseReference mandiRef) {
        Map<String, Object> batchUpdate = new HashMap<>();
        List<MandiPrice> sampleList = new ArrayList<>();
        boolean isHindi = isHindiLocale();

        if (isHindi) {
            sampleList.add(new MandiPrice("m1", "गेहूं", "पंजाब", "लुधियाना", "खन्ना मंडी",
                    2125.0, 2350.0, 2275.0, "क्विंटल", "2026-10-22", "पंजाब एगमार्कनेट पोर्टल", true));

            sampleList.add(new MandiPrice("m2", "धान (बासमती 1121)", "हरियाणा", "करनाल", "करनाल अनाज मंडी",
                    3400.0, 3950.0, 3750.0, "क्विंटल", "2026-10-22", "हरियाणा मंडी बोर्ड", true));

            sampleList.add(new MandiPrice("m3", "सरसों", "राजस्थान", "अलवर", "अलवर मंडी",
                    5100.0, 5650.0, 5450.0, "क्विंटल", "2026-10-22", "राजस्थान मंडी बोर्ड", true));

            sampleList.add(new MandiPrice("m4", "चना", "मध्य प्रदेश", "उज्जैन", "उज्जैन मंडी",
                    4800.0, 5400.0, 5150.0, "क्विंटल", "2026-10-22", "एमपी मंडी बोर्ड", true));

            sampleList.add(new MandiPrice("m5", "मक्का", "राजस्थान", "भीलवाड़ा", "भीलवाड़ा मंडी",
                    1850.0, 2200.0, 2050.0, "क्विंटल", "2026-10-22", "राजस्थान मंडी बोर्ड", true));

            sampleList.add(new MandiPrice("m6", "सोयाबीन", "मध्य प्रदेश", "इंदौर", "इंदौर मालवा मंडी",
                    4200.0, 4800.0, 4550.0, "क्विंटल", "2026-10-22", "एमपी राज्य बोर्ड", true));

            sampleList.add(new MandiPrice("m7", "आलू", "उत्तर प्रदेश", "आगरा", "आगरा मंडी",
                    1200.0, 1600.0, 1420.0, "क्विंटल", "2026-10-22", "यूपी मंडी परिषद", true));
        } else {
            sampleList.add(new MandiPrice("m1", "Wheat", "Punjab", "Ludhiana", "Khanna Mandi",
                    2125.0, 2350.0, 2275.0, "Qtl", "2026-10-22", "Punjab Agmarknet", true));

            sampleList.add(new MandiPrice("m2", "Paddy / Rice (Basmati 1121)", "Haryana", "Karnal", "Karnal Grain Market",
                    3400.0, 3950.0, 3750.0, "Qtl", "2026-10-22", "Haryana Mandi Board", true));

            sampleList.add(new MandiPrice("m3", "Mustard", "Rajasthan", "Alwar", "Alwar Mandi",
                    5100.0, 5650.0, 5450.0, "Qtl", "2026-10-22", "Rajasthan Mandi Board", true));

            sampleList.add(new MandiPrice("m4", "Gram / Chana", "Madhya Pradesh", "Ujjain", "Ujjain Mandi",
                    4800.0, 5400.0, 5150.0, "Qtl", "2026-10-22", "MP Mandi Board", true));

            sampleList.add(new MandiPrice("m5", "Maize", "Rajasthan", "Bhilwara", "Bhilwara Mandi",
                    1850.0, 2200.0, 2050.0, "Qtl", "2026-10-22", "Rajasthan Mandi Board", true));

            sampleList.add(new MandiPrice("m6", "Soybean", "Madhya Pradesh", "Indore", "Indore Malwa Mandi",
                    4200.0, 4800.0, 4550.0, "Qtl", "2026-10-22", "MP State Board", true));

            sampleList.add(new MandiPrice("m7", "Potato", "Uttar Pradesh", "Agra", "Agra Mandi",
                    1200.0, 1600.0, 1420.0, "Qtl", "2026-10-22", "UP Mandi Parishad", true));
        }

        for (MandiPrice item : sampleList) {
            batchUpdate.put(item.getRecordId(), item);
        }

        mandiRef.updateChildren(batchUpdate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mandiRef != null && mandiEventListener != null) {
            mandiRef.removeEventListener(mandiEventListener);
        }
    }
}
