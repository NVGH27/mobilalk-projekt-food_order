package com.example.foodorderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = RegisterActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;
    EditText lastNameET;
    EditText firstNameET;
    EditText emailET;
    EditText passwordET;
    EditText confirmPasswordET;

    private SharedPreferences preferences;
    private FirebaseAuth mauth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_register);

        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> insets);
        } else {
            throw new NullPointerException("Root view is null. Check your layout file.");
        }

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secret_key != SECRET_KEY) {
            finish();
        }

        lastNameET = findViewById(R.id.lastNameInput);
        firstNameET = findViewById(R.id.firstNameInput);
        emailET = findViewById(R.id.emailInput);
        passwordET = findViewById(R.id.passwordInput);
        confirmPasswordET = findViewById(R.id.confirmPasswordInput);
        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String email = preferences.getString("email", "");
        emailET.setText(email);

        mauth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Log.i(LOG_TAG, "onCreate");
    }

    private void startOrder() {
        Intent intent = new Intent(this, RestaurantListActivity.class);
        startActivity(intent);
        finish();
    }

    public void login(View view) {
        finish();
    }

    public void register(View view) {
        String firstName = firstNameET.getText().toString().trim();
        String lastName = lastNameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString();
        String confirmPassword = confirmPasswordET.getText().toString();

        if (firstName.isEmpty()) {
            firstNameET.setError("First name is required");
            firstNameET.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            lastNameET.setError("Last name is required");
            lastNameET.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailET.setError("Email is required");
            emailET.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordET.setError("Password is required");
            passwordET.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordET.setError("Confirm password is required");
            confirmPasswordET.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            passwordET.setError("Passwords do not match");
            passwordET.requestFocus();
            return;
        }

        Log.i(LOG_TAG, "Email: " + email);

        mauth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Registration successful!");
                            FirebaseUser user = mauth.getCurrentUser();
                            if (user != null) {
                                createUserInFirestore(user.getUid(), firstName, lastName);
                            }
                        } else {
                            Log.d(LOG_TAG, "Registration failed");
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void createUserInFirestore(String userId, String firstName, String lastName) {
        // Felhasználói adatok
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("createdAt", FieldValue.serverTimestamp());

        // Üres szállítási cím struktúra
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("addressLine1", "");
        shippingAddress.put("addressLine2", "");
        shippingAddress.put("city", "");
        shippingAddress.put("postalCode", "");
        userMap.put("shippingAddress", shippingAddress);

        userMap.put("birthDate", ""); // üres születésnap (később frissíthető)

        db.collection("Users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(LOG_TAG, "User document created in Firestore");

                    // Üres Favorites és Cart gyűjtemény létrehozása (egy üres dokumentummal)
                    createEmptyCollections(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error creating user document", e);
                    Toast.makeText(RegisterActivity.this, "Hiba a felhasználó mentésekor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void createEmptyCollections(String userId) {
        Map<String, Object> emptyMap = new HashMap<>();

        db.collection("Users").document(userId)
                .collection("Favorites").document("init")
                .set(emptyMap)
                .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Favorites collection created"))
                .addOnFailureListener(e -> Log.e(LOG_TAG, "Failed to create Favorites collection", e));

        db.collection("Users").document(userId)
                .collection("Cart").document("init")
                .set(emptyMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(LOG_TAG, "Cart collection created");
                    // Minden sikeres, indulhat a következő Activity
                    startOrder();
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Failed to create Cart collection", e);
                    Toast.makeText(RegisterActivity.this, "Hiba a kosár létrehozásakor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
