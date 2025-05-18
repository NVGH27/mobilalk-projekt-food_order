package com.example.foodorderapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListActivity extends AppCompatActivity {
    private static final String LOG_TAG = RestaurantListActivity.class.getName();

    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private List<Restaurant> restaurantList;
    private FirebaseFirestore db;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_restaurant_list);

        // Cím és animáció
        TextView title = findViewById(R.id.favoriteTitle);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        title.startAnimation(fadeIn);

        // Felhasználó bejelentkezve?
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Nincs bejelentkezve, irány a LoginActivity
            Log.d(LOG_TAG, "Nincs bejelentkezett felhasználó! Visszadob a loginra.");
            Intent loginIntent = new Intent(this, MainActivity.class);
            // Ezzel töröljük az Activity stack-et, hogy ne lehessen visszalépni a bejelentkezés nélkül
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }
        Log.d(LOG_TAG, "User is logged in: " + currentUser.getEmail());

        // RecyclerView inicializálása
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        restaurantList = new ArrayList<>();
        adapter = new RestaurantAdapter(this, restaurantList, restaurant -> {
            Intent intent = new Intent(RestaurantListActivity.this, MenuActivity.class);
            intent.putExtra("restaurantId", restaurant.getId()); // vagy bármilyen azonosító
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchRestaurantsFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
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

    private void fetchRestaurantsFromFirestore() {
        db.collection("Restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    restaurantList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Restaurant restaurant = doc.toObject(Restaurant.class);
                        if (restaurant != null) {
                            restaurantList.add(restaurant);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Log.d(LOG_TAG, "Restaurants loaded: " + restaurantList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error fetching restaurants", e);
                });
    }
}
