package com.example.kisansathi.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {

    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDatabase;

    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
        }
        return mDatabase;
    }

    public static DatabaseReference getUsersRef() {
        return getDatabase().getReference("users");
    }

    public static DatabaseReference getUserRef(String userId) {
        return getUsersRef().child(userId);
    }

    public static DatabaseReference getProductsRef() {
        return getDatabase().getReference("products");
    }

    public static DatabaseReference getMandiPricesRef() {
        return getDatabase().getReference("mandiPrices");
    }

    public static DatabaseReference getCropGuideRef() {
        return getDatabase().getReference("cropGuide");
    }

    public static DatabaseReference getFavoritesRef(String userId) {
        return getUserRef(userId).child("favorites");
    }

    public static void logout() {
        getAuth().signOut();
    }
}
