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

import com.example.kisansathi.adapters.CropAdapter;
import com.example.kisansathi.models.CropGuide;
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

public class CropGuideActivity extends AppCompatActivity {

    private EditText etSearchCrop;
    private Spinner spinnerCropSeason;
    private RecyclerView rvCropGuide;
    private ProgressBar progressBar;
    private TextView tvEmptyCropGuide;

    private CropAdapter cropAdapter;
    private final List<CropGuide> allCrops = new ArrayList<>();
    private final List<CropGuide> displayedCrops = new ArrayList<>();

    private String currentSearchQuery = "";
    private String selectedSeason = "All Seasons";

    private DatabaseReference cropRef;
    private ValueEventListener cropEventListener;
    private boolean isSeeding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_guide);

        etSearchCrop = findViewById(R.id.etSearchCrop);
        spinnerCropSeason = findViewById(R.id.spinnerCropSeason);
        rvCropGuide = findViewById(R.id.rvCropGuide);
        progressBar = findViewById(R.id.progressBarCrop);
        tvEmptyCropGuide = findViewById(R.id.tvEmptyCropGuide);

        setupRecyclerView();
        setupSeasonSpinner();
        setupSearchInput();
        loadCropGuidesFromFirebase();
    }

    private void setupRecyclerView() {
        rvCropGuide.setLayoutManager(new LinearLayoutManager(this));
        cropAdapter = new CropAdapter(this, displayedCrops);
        rvCropGuide.setAdapter(cropAdapter);
    }

    private void setupSeasonSpinner() {
        List<String> seasons = Arrays.asList(
                getString(R.string.all_seasons),
                getString(R.string.season_rabi),
                getString(R.string.season_kharif),
                getString(R.string.season_zaid)
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seasons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCropSeason.setAdapter(adapter);

        spinnerCropSeason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSeason = seasons.get(position);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSearchInput() {
        etSearchCrop.addTextChangedListener(new TextWatcher() {
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

    private void loadCropGuidesFromFirebase() {
        if (allCrops.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        cropRef = FirebaseUtil.getCropGuideRef();
        cropEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    if (!isSeeding) {
                        isSeeding = true;
                        populateSampleCropGuides(cropRef);
                    }
                    return;
                }

                isSeeding = false;
                List<CropGuide> temp = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CropGuide crop = itemSnapshot.getValue(CropGuide.class);
                    if (crop != null) {
                        temp.add(crop);
                    }
                }

                allCrops.clear();
                allCrops.addAll(temp);
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CropGuideActivity.this, "Error loading crop guides: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        cropRef.addValueEventListener(cropEventListener);
    }

    private boolean isHindiLocale() {
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        if (!current.isEmpty() && current.get(0) != null && current.get(0).getLanguage() != null) {
            return "hi".equalsIgnoreCase(current.get(0).getLanguage());
        }
        return false;
    }

    private void applyFilters() {
        displayedCrops.clear();
        String queryPattern = currentSearchQuery.toLowerCase(Locale.US).trim();
        String allSeasonsStr = getString(R.string.all_seasons);

        for (CropGuide c : allCrops) {
            boolean matchSeason = selectedSeason.equalsIgnoreCase(allSeasonsStr) ||
                    "All Seasons".equalsIgnoreCase(selectedSeason) ||
                    (c.getSeason() != null && c.getSeason().equalsIgnoreCase(selectedSeason));

            boolean matchSearch = queryPattern.isEmpty() ||
                    (c.getCropName() != null && c.getCropName().toLowerCase(Locale.US).contains(queryPattern)) ||
                    (c.getSoil() != null && c.getSoil().toLowerCase(Locale.US).contains(queryPattern)) ||
                    (c.getSeason() != null && c.getSeason().toLowerCase(Locale.US).contains(queryPattern));

            if (matchSeason && matchSearch) {
                displayedCrops.add(c);
            }
        }

        cropAdapter.setCropList(displayedCrops);

        if (displayedCrops.isEmpty()) {
            tvEmptyCropGuide.setVisibility(View.VISIBLE);
        } else {
            tvEmptyCropGuide.setVisibility(View.GONE);
        }
        rvCropGuide.setVisibility(View.VISIBLE);
    }

    private void populateSampleCropGuides(DatabaseReference cropRef) {
        Map<String, Object> batchUpdate = new HashMap<>();
        List<CropGuide> sampleList = new ArrayList<>();
        boolean isHindi = isHindiLocale();

        if (isHindi) {
            sampleList.add(new CropGuide("c1", "गेहूं", "रबी",
                    "उपजाऊ दोमट मिट्टी जिसमें पानी न रुके",
                    "1. गहरी जुताई के बाद 2 बार हैरो चलाएं।\n2. बीजोपचारित 40-45 किग्रा/एकड़ 20 सेमी की दूरी पर बोएं।",
                    "अवस्था अनुसार 4-6 सिंचाई करें (मुकुट जड़, कल्ले निकलना, फूल आना, दाना भरना)।",
                    "बुवाई के समय डीएपी तथा सिंचाई के बाद किस्तों में यूरिया दें।",
                    "दाने कठोर एवं सुनहरे होने पर कटाई करें।", true));

            sampleList.add(new CropGuide("c2", "धान / चावल", "खरीफ",
                    "पानी रोकने में सक्षम चिकनी दोमट मिट्टी",
                    "1. नर्सरी के लिए बीज 24 घंटे भिगोएं।\n2. 21-25 दिन की पौध खेत में रोपें।",
                    "वानस्पतिक वृद्धि में 2-5 सेमी पानी रखें; कटाई से 10 दिन पहले पानी निकालें।",
                    "यूरिया व जिंक सल्फेट तीन किस्तों में दें।",
                    "80% बालियां सुनहरी होने पर कटाई करें।", true));

            sampleList.add(new CropGuide("c3", "सरसों", "रबी",
                    "अच्छी जल निकासी वाली बलुई दोमट मिट्टी",
                    "1. 2 जुताई कर बारीक खेत तैयार करें।\n2. 1.5-2.0 किग्रा/एकड़ बीज 30 सेमी दूरी पर बोएं।",
                    "पहली सिंचाई 30 दिन में और दूसरी फली बनते समय (60 दिन) करें।",
                    "सल्फर हेतु एसएसपी एवं यूरिया दें।",
                    "फलियां पीली-भूरी होने पर कटाई करें।", true));

            sampleList.add(new CropGuide("c4", "चना", "रबी",
                    "अच्छी जल निकासी वाली दोमट मिट्टी",
                    "1. राइजोबियम से बीजोपचार करें।\n2. 25-30 किग्रा/एकड़ 30 सेमी दूरी पर बोएं।",
                    "आवश्यकतानुसार फूल आने से पूर्व एक हल्की सिंचाई करें।",
                    "बुवाई के समय डीएपी/एसएसपी खाद दें।",
                    "पौधे सूखने व पत्तियां भूरी होने पर कटाई करें।", true));

            sampleList.add(new CropGuide("c5", "मक्का", "खरीफ / ज़ायद",
                    "अच्छी जल निकासी वाली दोमट मिट्टी",
                    "1. 2-3 जुताई से खेत तैयार करें।\n2. 8 किग्रा/एकड़ बीज 60 सेमी दूरी पर बोएं।",
                    "घुटने तक ऊंचाई और मूंछ आने पर सिंचाई करें।",
                    "मिट्टी की जांच के अनुसार संतुलित एनपीके खाद दें।",
                    "भुट्टे के छिलके सूखने पर कटाई करें।", true));

            sampleList.add(new CropGuide("c6", "सोयाबीन", "खरीफ",
                    "अच्छी दोमट या काली मिट्टी",
                    "1. ब्रेडीराइजोबियम कल्चर से बीजोपचार करें।\n2. 30 किग्रा/एकड़ बीज 45 सेमी दूरी पर बोएं।",
                    "फूल व फली बनते समय आवश्यकतानुसार सिंचाई दें।",
                    "बुवाई के समय फास्फोरस व पोटाश खाद दें।",
                    "पत्तियां झड़ने व फलियां पीली-भूरी होने पर कटाई करें।", true));
        } else {
            sampleList.add(new CropGuide("c1", "Wheat", "Rabi",
                    "Well-drained fertile clay loam",
                    "1. Deep ploughing followed by 2 harrowings.\n2. Sow treated seeds 40-45 kg/acre in rows 20 cm apart.",
                    "Provide 4-6 irrigations at stage intervals: Crown Root Initiation (21 days), Tillering, Flowering, Grain Filling.",
                    "Apply recommended DAP at basal sowing and Urea in split doses after irrigation.",
                    "Harvest when grains mature and leaves turn golden yellow (moisture < 14%).", true));

            sampleList.add(new CropGuide("c2", "Paddy / Rice", "Kharif",
                    "Clay loam water-retentive soil",
                    "1. Soak seeds 24 hrs for nursery bed.\n2. Transplant 21-25 day old seedlings into puddled field.",
                    "Maintain 2-5 cm standing water in vegetative stage; drain 10 days before harvest.",
                    "Apply Nitrogen in 3 splits (transplanting, tillering, panicle initiation) along with Zinc Sulfate.",
                    "Harvest when 80% panicles turn golden yellow.", true));

            sampleList.add(new CropGuide("c3", "Mustard", "Rabi",
                    "Light to medium loamy soil with good drainage",
                    "1. Fine seedbed with 2 ploughings.\n2. Sow seeds 1.5-2.0 kg/acre at 30 cm row spacing.",
                    "First irrigation at 30 days (flowering) and second at pod formation (60 days).",
                    "Apply Single Super Phosphate (SSP) for Sulfur supply along with Urea.",
                    "Harvest when pods turn yellowish brown.", true));

            sampleList.add(new CropGuide("c4", "Gram / Chickpea", "Rabi",
                    "Well-drained sandy loam to clay loam",
                    "1. Treat seed with Rhizobium.\n2. Sow 25-30 kg/acre seeds at 30 cm spacing.",
                    "Usually rainfed; provide 1 protective irrigation at pre-flowering stage if dry.",
                    "Apply basal Phosphatic fertilizer (DAP/SSP) during sowing.",
                    "Harvest when plants dry and leaves turn reddish-brown.", true));

            sampleList.add(new CropGuide("c5", "Maize", "Kharif / Summer",
                    "Well-drained fertile loamy soil",
                    "1. Prepare seedbed with 2-3 ploughings.\n2. Sow 8 kg/acre seeds in rows 60 cm apart.",
                    "Provide critical irrigations at knee-high, tasseling, and silking stages.",
                    "Apply NPK 120:60:40 per hectare as per soil recommendation.",
                    "Harvest when cob outer husks turn dry and yellow.", true));

            sampleList.add(new CropGuide("c6", "Soybean", "Kharif",
                    "Well-drained fertile black cotton or loamy soil",
                    "1. Treat seeds with Bradyrhizobium culture.\n2. Sow 30 kg/acre seeds at 45 cm row spacing.",
                    "Requires good seasonal rain; provide protective irrigation during flowering if dry.",
                    "Apply Phosphatic & Potassic basal fertilizers at sowing time.",
                    "Harvest when leaves drop off and pods turn golden brown.", true));
        }

        for (CropGuide c : sampleList) {
            batchUpdate.put(c.getCropId(), c);
        }

        cropRef.updateChildren(batchUpdate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cropRef != null && cropEventListener != null) {
            cropRef.removeEventListener(cropEventListener);
        }
    }
}
