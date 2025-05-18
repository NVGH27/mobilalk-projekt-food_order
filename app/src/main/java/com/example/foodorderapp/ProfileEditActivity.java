package com.example.foodorderapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText firstNameEdit, lastNameEdit, birthDateEdit;
    private EditText address1Edit, address2Edit, cityEdit, postalCodeEdit;
    private Button saveButton, logoutButton, deleteButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null || currentUser.isAnonymous()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        loadUserData();

        birthDateEdit.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> saveUserData());
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileEditActivity.this, MainActivity.class));
            finish();
        });
        deleteButton.setOnClickListener(v -> deleteAccount());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, RestaurantListActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileEditActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    private void initViews() {
        firstNameEdit = findViewById(R.id.editFirstName);
        lastNameEdit = findViewById(R.id.editLastName);
        birthDateEdit = findViewById(R.id.editBirthDate);
        address1Edit = findViewById(R.id.editAddressLine1);
        address2Edit = findViewById(R.id.editAddressLine2);
        cityEdit = findViewById(R.id.editCity);
        postalCodeEdit = findViewById(R.id.editPostalCode);

        saveButton = findViewById(R.id.buttonSave);
        logoutButton = findViewById(R.id.buttonLogout);
        deleteButton = findViewById(R.id.buttonDeleteAccount);
    }

    private void loadUserData() {
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                firstNameEdit.setText(snapshot.getString("firstName"));
                lastNameEdit.setText(snapshot.getString("lastName"));
                birthDateEdit.setText(snapshot.getString("birthDate"));

                Map<String, Object> shipping = (Map<String, Object>) snapshot.get("shippingAddress");
                if (shipping != null) {
                    address1Edit.setText((String) shipping.get("addressLine1"));
                    address2Edit.setText((String) shipping.get("addressLine2"));
                    cityEdit.setText((String) shipping.get("city"));
                    postalCodeEdit.setText((String) shipping.get("postalCode"));
                }
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            birthDateEdit.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveUserData() {
        Map<String, Object> data = new HashMap<>();

        String firstName = firstNameEdit.getText().toString().trim();
        if (!firstName.isEmpty()) data.put("firstName", firstName);

        String lastName = lastNameEdit.getText().toString().trim();
        if (!lastName.isEmpty()) data.put("lastName", lastName);

        String birthDate = birthDateEdit.getText().toString().trim();
        if (!birthDate.isEmpty()) data.put("birthDate", birthDate);

        Map<String, Object> address = new HashMap<>();
        String addressLine1 = address1Edit.getText().toString().trim();
        if (!addressLine1.isEmpty()) address.put("addressLine1", addressLine1);

        String addressLine2 = address2Edit.getText().toString().trim();
        if (!addressLine2.isEmpty()) address.put("addressLine2", addressLine2);

        String city = cityEdit.getText().toString().trim();
        if (!city.isEmpty()) address.put("city", city);

        String postalCode = postalCodeEdit.getText().toString().trim();
        if (!postalCode.isEmpty()) address.put("postalCode", postalCode);

        if (!address.isEmpty()) {
            data.put("shippingAddress", address);
        }

        if (data.isEmpty()) {
            Toast.makeText(this, "Nincs módosítás!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users").document(currentUser.getUid())
                .update(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Mentve!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);    // fontos, hogy ez itt legyen
                    finish();               // itt zárjuk az activity-t
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hiba mentéskor!", Toast.LENGTH_SHORT).show());

    }


    private void deleteAccount() {
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());

        docRef.delete().addOnSuccessListener(aVoid -> {
            currentUser.delete().addOnSuccessListener(unused -> {
                Toast.makeText(this, "Fiók törölve", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }).addOnFailureListener(e -> Toast.makeText(this, "Hiba történt!", Toast.LENGTH_SHORT).show());
    }
}
