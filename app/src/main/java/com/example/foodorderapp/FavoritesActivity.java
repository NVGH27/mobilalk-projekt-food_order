package com.example.foodorderapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";

    RecyclerView favoritesRecyclerView;
    FavoritesAdapter favoritesAdapter;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_favorites);

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoritesAdapter = new FavoritesAdapter(new ArrayList<>());
        favoritesRecyclerView.setAdapter(favoritesAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadFavoritesFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, RestaurantListActivity.class));
                return true;
            } else if (itemId == R.id.nav_favorites) {
                return true; // már ezen az oldalon vagyunk
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    private void loadFavoritesFromFirestore() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Felhasználó nincs bejelentkezve", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .document(userId)
                .collection("Favorites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<FavoriteItem> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String imageUrl = document.getString("imageUrl");
                            Long priceLong = document.getLong("price");
                            String documentId = document.getId();

                            if (name != null && imageUrl != null && priceLong != null) {
                                int price = priceLong.intValue();
                                newList.add(new FavoriteItem(name, price, imageUrl, documentId));
                            }
                        }
                        if (newList.isEmpty()) {
                            Toast.makeText(this, "Nincsenek kedvenc ételek", Toast.LENGTH_SHORT).show();
                        }
                        favoritesAdapter.updateList(newList);
                    } else {
                        Toast.makeText(this, "Hiba a kedvencek betöltésekor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
