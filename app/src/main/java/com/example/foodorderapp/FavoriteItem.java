package com.example.foodorderapp;

public class FavoriteItem {
    private String name;
    private int price;
    private String imageUrl;
    private String documentId;

    public FavoriteItem(String name, int price, String imageUrl, String documentId) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDocumentId() {
        return documentId;
    }
}
