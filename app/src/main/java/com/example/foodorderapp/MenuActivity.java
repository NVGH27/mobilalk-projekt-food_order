package com.example.foodorderapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private static final String LOG_TAG = MenuActivity.class.getName();

    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<MenuItem> menuList;

    private FirebaseFirestore db;
    private String restaurantId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        TextView menuTitle = findViewById(R.id.menuTitle);
        recyclerView = findViewById(R.id.menuRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuList = new ArrayList<>();
        adapter = new MenuAdapter(this, menuList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId == null) {
            Toast.makeText(this, "Nincs étterem ID megadva", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("Restaurants")
                .document(restaurantId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        menuTitle.setText(name + " menüje");
                    }
                });

        fetchMenuItems();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
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
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchMenuItems() {
        db.collection("Restaurants")
                .document(restaurantId)
                .collection("Menu")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    menuList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        MenuItem item = doc.toObject(MenuItem.class);
                        if (item != null) {
                            menuList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Log.d(LOG_TAG, "Menu items loaded: " + menuList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error fetching menu items", e);
                });
    }

}
