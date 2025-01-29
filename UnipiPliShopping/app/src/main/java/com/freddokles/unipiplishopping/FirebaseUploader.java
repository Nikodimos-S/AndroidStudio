package com.freddokles.unipiplishopping;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseUploader {

    private static final String COLLECTION_NAME = "items";

    // Method to clear the collection
    public static void clearCollection(FirebaseFirestore db, Runnable onComplete) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        db.collection(COLLECTION_NAME).document(document.getId()).delete();
                    }
                    System.out.println("Collection cleared!");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error clearing collection: " + e.getMessage());
                });
    }

    // Method to upload the data
    public static void uploadData(FirebaseFirestore db) {
        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        // Define the stores
        String[] stores = {"TechHaven", "GreenLife Emporium"};

        // Define the products
        Object[][] products = {
                {"Quantum Ultra Wireless Headphones", "Noise-canceling over-ear headphones with 30-hour battery life and Bluetooth 5.3. Perfect for music lovers and gamers.", "2023-12-15T00:00:00", 149.99, stores[0]},
                {"EcoSphere Reusable Water Bottle", "1-liter insulated stainless-steel bottle, keeps drinks cold for 24 hours and hot for 12 hours. Available in 5 colors.", "2024-04-01T00:00:00", 24.99, stores[1]},
                {"Pixel Pro Drawing Tablet", "A professional drawing tablet with a 10.5-inch display, 4096 pressure levels, and lightweight stylus. Ideal for digital artists.", "2024-07-10T00:00:00", 199.99, stores[0]},
                {"Arcadia Gaming Mouse", "Ergonomic gaming mouse with 12 programmable buttons, customizable RGB lighting, and a 20,000 DPI sensor.", "2023-11-05T00:00:00", 59.99, stores[1]},
                {"SolarSmart LED Desk Lamp", "A dimmable LED desk lamp with a built-in solar charging panel and USB charging port. Perfect for eco-friendly workspaces.", "2024-02-18T00:00:00", 39.99, stores[0]},
                {"Virtuo VR Headset", "A lightweight VR headset with a 120-degree field of view and built-in spatial audio for immersive gaming experiences.", "2024-03-25T00:00:00", 299.99, stores[1]},
                {"NutriBlend Smart Blender", "A compact blender with smart presets for smoothies, soups, and juices. Features a self-cleaning mode and touchscreen controls.", "2023-10-20T00:00:00", 89.99, stores[0]},
                {"PulseFit Smartwatch", "Fitness-focused smartwatch with heart rate monitoring, sleep tracking, and GPS. Water-resistant up to 50m.", "2023-08-12T00:00:00", 129.99, stores[1]},
                {"GreenGrow Indoor Herb Kit", "A beginner-friendly hydroponic kit for growing fresh herbs like basil and mint indoors. Includes LED grow lights.", "2024-01-10T00:00:00", 49.99, stores[0]},
                {"RetroByte Portable Console", "A handheld console with preloaded classic games from the 80s and 90s, plus support for microSD card expansions.", "2024-06-15T00:00:00", 89.99, stores[1]}
        };

        // Loop through the products and add them to Firestore
        for (Object[] product : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", product[0]);
            item.put("description", product[1]);

            // Parse the release date string into a Date object
            try {
                Date releaseDate = dateFormat.parse((String) product[2]);
                item.put("release_date", releaseDate);
            } catch (ParseException e) {
                System.err.println("Error parsing date for product: " + product[0] + ". Skipping...");
                continue; // Skip this product if the date format is invalid
            }

            item.put("price", product[3]);
            item.put("store_id", product[4]);

            db.collection(COLLECTION_NAME)
                    .add(item)
                    .addOnSuccessListener(documentReference -> {
                        System.out.println("Item added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Error adding item: " + e.getMessage());
                    });
        }
    }
}
