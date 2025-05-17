package com.example.foodorderapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private List<FavoriteItem> favoriteList;

    public FavoritesAdapter(List<FavoriteItem> favoriteList) {
        this.favoriteList = favoriteList;
    }

    public void updateList(List<FavoriteItem> newList) {
        this.favoriteList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        FavoriteItem item = favoriteList.get(position);
        Context context = holder.itemView.getContext();

        holder.nameTextView.setText(item.getName());
        holder.priceTextView.setText(item.getPrice() + " Ft");
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageView);

        holder.removeButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(userId)
                    .collection("Favorites")
                    .document(item.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        favoriteList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Eltávolítva a kedvencekből", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Nem sikerült eltávolítani", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;
        ImageView imageView;
        ImageButton removeButton;

        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.favoriteName);
            priceTextView = itemView.findViewById(R.id.favoritePrice);
            imageView = itemView.findViewById(R.id.favoriteImage);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
