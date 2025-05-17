package com.example.foodorderapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private Context context;
    private List<MenuItem> menuList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public MenuAdapter(Context context, List<MenuItem> menuList) {
        this.context = context;
        this.menuList = menuList;
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuList.get(position);

        holder.nameText.setText(item.getName());
        holder.descriptionText.setText(item.getDescription());
        holder.priceText.setText(String.format("%.0f Ft", item.getPrice()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageView);

        holder.cartButton.setOnClickListener(v -> addToCollection("Cart", item));
        holder.favButton.setOnClickListener(v -> addToCollection("Favorites", item));
    }

    private void addToCollection(String collectionName, MenuItem item) {
        if (currentUser == null) {
            Toast.makeText(context, "Jelentkezz be először!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .document(currentUser.getUid())
                .collection(collectionName)
                .document(item.getName())
                .set(item)
                .addOnSuccessListener(unused -> Toast.makeText(context, "Hozzáadva a(z) " + collectionName.toLowerCase() + "-hoz", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, descriptionText, priceText;
        ImageView imageView;
        ImageView favButton, cartButton;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.menuItemName);
            descriptionText = itemView.findViewById(R.id.menuItemDescription);
            priceText = itemView.findViewById(R.id.menuItemPrice);
            imageView = itemView.findViewById(R.id.menuItemImage);
            favButton = itemView.findViewById(R.id.favoriteButton);
            cartButton = itemView.findViewById(R.id.cartButton);
        }
    }
}
