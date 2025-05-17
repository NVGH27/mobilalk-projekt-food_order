package com.example.foodorderapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_PROFILE = 100;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView textFullName, textBirthDate, textAddress;
    private Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        textFullName = findViewById(R.id.textFullName);
        textBirthDate = findViewById(R.id.textBirthDate);
        textAddress = findViewById(R.id.textAddress);
        btnEdit = findViewById(R.id.btnEdit);

        loadUserProfile();

        btnEdit.setOnClickListener(v -> openProfileEdit());

        // BottomNavigationView setup
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
                return true;
            }
            return false;
        });
    }

    private void openProfileEdit() {
        Intent intent = new Intent(this, ProfileEditActivity.class);
        startActivityForResult(intent, REQUEST_EDIT_PROFILE);
    }

    @SuppressLint("SetTextI18n")
    private void loadUserProfile() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String birthDate = documentSnapshot.getString("birthDate");

                        Map<String, Object> address = (Map<String, Object>) documentSnapshot.get("shippingAddress");
                        String addressLine1 = address != null ? (String) address.get("addressLine1") : "";
                        String addressLine2 = address != null ? (String) address.get("addressLine2") : "";
                        String city = address != null ? (String) address.get("city") : "";
                        String postalCode = address != null ? (String) address.get("postalCode") : "";

                        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                        String fullAddress = addressLine1 + ", " + addressLine2 + ", " + city + ", " + postalCode;

                        textFullName.setText("Név: " + fullName);
                        textBirthDate.setText("Születési dátum: " + birthDate);
                        textAddress.setText("Cím: " + fullAddress);
                    } else {
                        Toast.makeText(this, "Nincs adat elmentve", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Frissítjük az adatokat a profil nézetben, ha az editből visszatértünk
            loadUserProfile();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public void scheduleCouponAlarm(Context context, long triggerAtMillis) {
        Intent intent = new Intent(context, CouponAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

}
