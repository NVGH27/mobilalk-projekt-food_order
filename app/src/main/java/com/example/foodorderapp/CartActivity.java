package com.example.foodorderapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView cartRecyclerView;
    CartAdapter cartAdapter;
    FirebaseFirestore db;
    FirebaseAuth auth;
    Button btnPay;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(new ArrayList<>());
        cartRecyclerView.setAdapter(cartAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadCartFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else {
                return false;
            }
        });
        btnPay = findViewById(R.id.btnPay);

        btnPay.setOnClickListener(v -> {
            payAndClearCart();
        });
    }

    private void payAndClearCart() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Felhasználó nincs bejelentkezve", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .document(userId)
                .collection("Cart")
                .orderBy("price")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            db.collection("Users")
                                    .document(userId)
                                    .collection("Cart")
                                    .document(doc.getId())
                                    .delete();
                        }
                        cartAdapter.updateList(new ArrayList<>());
                        Toast.makeText(this, "Fizetés sikeres, a kosár kiürült", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Hiba történt a fizetés során", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCartFromFirestore() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Felhasználó nincs bejelentkezve", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .document(userId)
                .collection("Cart")
                .orderBy("price")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CartItem> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String imageUrl = document.getString("imageUrl");
                            Long priceLong = document.getLong("price");
                            String documentId = document.getId();

                            if (name != null && imageUrl != null && priceLong != null) {
                                int price = priceLong.intValue();
                                newList.add(new CartItem(name, price, imageUrl, documentId));
                            }
                        }
                        if (newList.isEmpty()) {
                            Toast.makeText(this, "A kosár üres", Toast.LENGTH_SHORT).show();
                        }
                        cartAdapter.updateList(newList);
                    } else {
                        Toast.makeText(this, "Hiba a kosár betöltésekor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        cartAdapter.notifyDataSetChanged();
    }
}
