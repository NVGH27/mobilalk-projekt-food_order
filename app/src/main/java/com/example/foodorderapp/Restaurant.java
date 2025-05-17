package com.example.foodorderapp;

public class Restaurant {
    private String id;
    private String name;
    private String location;
    private double rating;
    private String imageUrl;

    public Restaurant() {} // Szükséges Firestore-hoz

    public Restaurant(String id, String name, String location, double rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
