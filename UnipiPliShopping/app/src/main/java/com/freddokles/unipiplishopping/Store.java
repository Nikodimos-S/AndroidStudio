package com.freddokles.unipiplishopping;

import com.google.firebase.firestore.GeoPoint;

public class Store {
    private String name;
    private GeoPoint location;

    public Store(String name, GeoPoint location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getLocation() {
        return location;
    }
}
