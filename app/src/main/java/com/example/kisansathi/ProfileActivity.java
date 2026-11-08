package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.kisansathi.models.User;
import com.example.kisansathi.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private TextView tvProfileDetailName, tvProfileDetailEmail;
    private RadioGroup rgLanguage;
    private RadioButton rbEnglish, rbHindi;
    private MaterialButton btnProfileLogout;
    private boolean isSettingInitialLanguage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseUtil.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileDetailName = findViewById(R.id.tvProfileDetailName);
        tvProfileDetailEmail = findViewById(R.id.tvProfileDetailEmail);

        rgLanguage = findViewById(R.id.rgLanguage);
        rbEnglish = findViewById(R.id.rbEnglish);
        rbHindi = findViewById(R.id.rbHindi);
        btnProfileLogout = findViewById(R.id.btnProfileLogout);

        loadProfileData(currentUser);
        setupLanguageSelector();

        btnProfileLogout.setOnClickListener(v -> performLogout());
    }

    private void loadProfileData(FirebaseUser currentUser) {
        String uid = currentUser.getUid();
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "N/A";

        tvProfileEmail.setText(email);
        tvProfileDetailEmail.setText(email);

        FirebaseUtil.getUserRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !TextUtils.isEmpty(user.getName())) {
                        tvProfileName.setText(user.getName());
                        tvProfileDetailName.setText(user.getName());
                        return;
                    }
                }
                String defaultName = getString(R.string.farmer);
                tvProfileName.setText(defaultName);
                tvProfileDetailName.setText(defaultName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                String defaultName = getString(R.string.farmer);
                tvProfileName.setText(defaultName);
                tvProfileDetailName.setText(defaultName);
            }
        });
    }

    private void setupLanguageSelector() {
        isSettingInitialLanguage = true;

        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        String currentLang = "en";
        if (!currentLocales.isEmpty() && currentLocales.get(0) != null) {
            String lang = currentLocales.get(0).getLanguage();
            if (lang != null) {
                currentLang = lang;
            }
        }

        if (currentLang.equalsIgnoreCase("hi")) {
            rbHindi.setChecked(true);
        } else {
            rbEnglish.setChecked(true);
        }

        isSettingInitialLanguage = false;

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            if (isSettingInitialLanguage) return;

            String selectedLang = (checkedId == R.id.rbHindi) ? "hi" : "en";
            LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
            String activeLang = (!current.isEmpty() && current.get(0) != null) ? current.get(0).getLanguage() : "en";

            if (!selectedLang.equalsIgnoreCase(activeLang)) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selectedLang));
            }
        });
    }

    private void performLogout() {
        FirebaseUtil.logout();
        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
