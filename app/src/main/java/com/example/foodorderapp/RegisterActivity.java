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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_register);

        View rootView = findViewById(android.R.id.content); // Replace with the correct ID if needed
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                // Handle window insets if needed
                return insets;
            });
        } else {
            throw new NullPointerException("Root view is null. Check your layout file.");
        }

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secret_key != 99) {
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

        Log.i(LOG_TAG, "onCreate");
    }

    private void startOrder(/*user data*/){
        Intent intent = new Intent(this, RestaurantListActivity.class);
        //intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    public void login(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    public void register(View view) {
        String firstName = firstNameET.getText().toString();
        String lastName = lastNameET.getText().toString();
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String confirmPassword = confirmPasswordET.getText().toString();

        if(!password.equals(confirmPassword)) {
            Log.e(LOG_TAG, "Passwords do not match");
            return;
        }

        Log.i(LOG_TAG, "Email: " + email);
        // startOrder();
        mauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "Regisztráció sikeres!");
                    startOrder();
                } else {
                    Log.d(LOG_TAG, "Sikertelen regisztráció");
                    Toast.makeText(RegisterActivity.this, "Regisztráció sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
