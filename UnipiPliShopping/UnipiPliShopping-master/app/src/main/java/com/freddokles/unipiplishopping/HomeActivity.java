package com.freddokles.unipiplishopping;

import android.Manifest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;



import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth m_auth;
    private FirebaseFirestore db;
    // List to store document names and prices
    private final List<String> documentNames = new ArrayList<>();
    private final List<Double> prices = new ArrayList<>();
    private String userDocumentId; // Current user's document ID



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        loadLocale();
        loadTheme();

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            apiAvailability.getErrorDialog(this, resultCode, 9000).show();
        }


        /*Uploads firebase files for testing
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUploader.clearCollection(db, () -> FirebaseUploader.uploadData(db));*/

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        m_auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fetchItems();
        // Get the current user's document
        getUserDocument();
        createNotificationChannel();
        checkAndRequestNotificationPermission();
        checkAndRequestLocationPermission();

        if (isFeatureEnabled()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            startService(serviceIntent);
        }

        ImageView settingsBtn = findViewById(R.id.settingsButton);
        ImageView cartBtn = findViewById(R.id.cartButton);
        ImageView homeBtn = findViewById(R.id.homeButton);
        homeBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
        });

        settingsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        cartBtn.setOnClickListener(view -> openCartActivity());
    }
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    private void checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "Location Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    private void getUserDocument() {
        String currentUserEmail = m_auth.getCurrentUser().getEmail(); // Get the current user's email

        db.collection("users")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userDocumentId = document.getId(); // Get the current user's document ID
                            break;
                        }
                    } else {
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchItems() {
        db.collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            Double price = document.getDouble("price");

                            if (name != null && price != null) {
                                documentNames.add(name);
                                prices.add(price);
                            }
                        }

                        // Set up buttons after fetching items
                        setupButtons();
                    }
                });
    }
    private void setupButtons() {
        // Loop through the buttons and assign OnClickListeners
        for (int i = 1; i <= 10; i++) {
            int buttonId = getResources().getIdentifier("buy_btn_" + i, "id", getPackageName());
            Button button = findViewById(buttonId);

            if (button != null) {
                int index = i - 1; // Index for documentNames and prices

                button.setOnClickListener(view -> {
                    String documentName = documentNames.get(index);
                    Double price = prices.get(index);

                    // Save the updated cart data to Firestore
                    saveCartDataToFirestore(documentName, price);
                });
            }
        }
    }

    private void saveCartDataToFirestore(String itemName, Double price) {
        if (userDocumentId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(userDocumentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Fetch the current cart
                        List<HashMap<String, Object>> cart =
                                (List<HashMap<String, Object>>) task.getResult().get("cart");
                        if (cart == null) {
                            cart = new ArrayList<>();
                        }

                        // Check if the item already exists in the cart
                        boolean itemExists = false;
                        for (HashMap<String, Object> cartItem : cart) {
                            if (cartItem.get("name").equals(itemName)) {
                                // Increment the quantity of the existing item
                                int currentQuantity = ((Long) cartItem.get("quantity")).intValue();
                                cartItem.put("quantity", currentQuantity + 1);
                                itemExists = true;
                                break;
                            }
                        }

                        // If the item is not in the cart, add it with quantity 1
                        if (!itemExists) {
                            HashMap<String, Object> newItem = new HashMap<>();
                            newItem.put("name", itemName);
                            newItem.put("quantity", 1);
                            newItem.put("price", price);
                            cart.add(newItem);
                        }

                        // Save the updated cart back to Firestore
                        db.collection("users")
                                .document(userDocumentId)
                                .update("cart", cart)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, itemName + " added to cart!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update cart!", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Failed to fetch cart data!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openCartActivity() {
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    private boolean isFeatureEnabled(){
        return getSharedPreferences("Settings", MODE_PRIVATE)
                .getBoolean("Geofence_Enabled", false);
    }


    private void setTheme(boolean toggle_state) {

        if (toggle_state) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    private void loadTheme() {
        boolean toggle_theme = getSharedPreferences("Settings", MODE_PRIVATE)
                .getBoolean("My_State", true); // Default to English
        setTheme(toggle_theme);
    }
    private void loadLocale() {
        String languageCode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", "en"); // Default to English
        setLocale(languageCode);
    }
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale currentLocale = Locale.getDefault();

        if (!currentLocale.getLanguage().equals(languageCode)) {
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());

            //saveLocale(languageCode);

            recreate();
        }
    }

    private void saveLocale(String languageCode) {
        getSharedPreferences("Settings", MODE_PRIVATE)
                .edit()
                .putString("My_Lang", languageCode)
                .apply();
    }
}



