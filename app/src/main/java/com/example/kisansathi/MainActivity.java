package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kisansathi.models.User;
import com.example.kisansathi.utils.FirebaseUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeUser;
    private ImageButton btnDashboardLogout;
    private MaterialCardView cardProducts, cardMandi,
            cardCropGuide, cardFavorites, cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseUtil.getCurrentUser();

        if (currentUser == null) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        btnDashboardLogout = findViewById(R.id.btnDashboardLogout);

        cardProducts = findViewById(R.id.cardProducts);
        cardMandi = findViewById(R.id.cardMandi);
        cardCropGuide = findViewById(R.id.cardCropGuide);
        cardFavorites = findViewById(R.id.cardFavorites);
        cardProfile = findViewById(R.id.cardProfile);

        loadUserProfile(currentUser);

        btnDashboardLogout.setOnClickListener(v -> performLogout());

        cardProducts.setOnClickListener(v ->
                startActivity(new Intent(this, ProductCategoryActivity.class)));

        cardMandi.setOnClickListener(v ->
                startActivity(new Intent(this, MandiPriceActivity.class)));

        cardCropGuide.setOnClickListener(v ->
                startActivity(new Intent(this, CropGuideActivity.class)));

        cardFavorites.setOnClickListener(v ->
                startActivity(new Intent(this, FavoritesActivity.class)));

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadUserProfile(FirebaseUser currentUser) {
        String userId = currentUser.getUid();

        tvWelcomeUser.setText(
                getString(R.string.welcome_farmer,
                        getString(R.string.farmer))
        );

        FirebaseUtil.getUserRef(userId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                if (isFinishing() || isDestroyed()) {
                                    return;
                                }

                                String name = null;

                                if (snapshot.exists()) {
                                    User user = snapshot.getValue(User.class);

                                    if (user != null
                                            && !TextUtils.isEmpty(user.getName())) {
                                        name = user.getName();
                                    }
                                }

                                if (TextUtils.isEmpty(name)) {
                                    name = currentUser.getDisplayName();
                                }

                                if (TextUtils.isEmpty(name)) {
                                    name = currentUser.getEmail();
                                }

                                if (TextUtils.isEmpty(name)) {
                                    name = getString(R.string.farmer);
                                }

                                tvWelcomeUser.setText(
                                        getString(R.string.welcome_farmer, name));
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {
                                if (!isFinishing() && !isDestroyed()) {
                                    tvWelcomeUser.setText(
                                            getString(R.string.welcome_farmer,
                                                    getString(R.string.farmer)));
                                }
                            }
                        });
    }

    private void performLogout() {
        FirebaseUtil.logout();

        Toast.makeText(
                this,
                getString(R.string.logout_success),
                Toast.LENGTH_SHORT
        ).show();

        openLogin();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
