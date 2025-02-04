package com.freddokles.unipiplishopping;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnQuantityChangeListener {
    FirebaseAuth m_auth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CartAdapter adapter;

    private List<String> productNames = new ArrayList<>();
    private HashMap<String, Integer> itemCounts = new HashMap<>();
    private List<Double> productPrices = new ArrayList<>();

    private TextView totalPriceText;
    private Button orderButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        m_auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadLocale();
        loadTheme();
        totalPriceText = findViewById(R.id.totalPriceText);
        orderButton = findViewById(R.id.orderButton);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderButton.setOnClickListener(view -> placeOrder());

        loadCartData();

        ImageView homeBtn = findViewById(R.id.homeButton);
        ImageView settingsBtn = findViewById(R.id.settingsButton);

        homeBtn.setOnClickListener(view -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        settingsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(CartActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


    }
    //fethes user email from firebase auth and then uses that to fetch the cart from the firestore database
    private void loadCartData() {
        String userEmail = m_auth.getCurrentUser().getEmail();

        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String userId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists() && documentSnapshot.contains("cart")) {
                                        List<Map<String, Object>> cartItems =
                                                (List<Map<String, Object>>) documentSnapshot.get("cart");

                                        // Clear the existing lists
                                        productNames.clear();
                                        productPrices.clear();
                                        itemCounts.clear();
                                        //gets item name price and quantity from firestore and adds it to respective list
                                        if (cartItems != null) {
                                            for (Map<String, Object> item : cartItems) {
                                                String name = (String) item.get("name");
                                                double price = (Double) item.get("price");
                                                int quantity = ((Long) item.get("quantity")).intValue();

                                                productNames.add(name);
                                                productPrices.add(price);
                                                itemCounts.put(name, quantity);
                                            }
                                        }

                                        // Initialize or refresh the adapter
                                        if (adapter == null) {
                                            adapter = new CartAdapter(this, productNames, itemCounts, productPrices, this);
                                            recyclerView.setAdapter(adapter);
                                        } else {
                                            adapter.notifyDataSetChanged();
                                        }

                                        updateTotalPrice();
                                    } else {
                                        Log.d("CartActivity", "No cart field found in Firestore document.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CartActivity", "Error fetching user document: ", e);
                                });
                    } else {
                        Log.d("CartActivity", "No user found with the provided email.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartActivity", "Error querying Firestore: ", e);
                });
    }

    private void updateTotalPrice() {
        double total = 0.0;
        for (int i = 0; i < productNames.size(); i++) {
            total += productPrices.get(i) * itemCounts.get(productNames.get(i));
        }
        totalPriceText.setText(String.format(Locale.getDefault(), "Total: €%.2f", total));
    }

    //this method gets the user's email, fethes the cart then adds the order data to firestore,
    //clears the cart in the database and the cart activity(zeros the total and removes all items)
    private void placeOrder() {
        String userEmail = m_auth.getCurrentUser().getEmail();

        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String userId = querySnapshot.getDocuments().get(0).getId();

                        //order data
                        List<Map<String, Object>> orderItems = new ArrayList<>();
                        for (int i = 0; i < productNames.size(); i++) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", productNames.get(i));
                            item.put("price", productPrices.get(i));
                            item.put("quantity", itemCounts.get(productNames.get(i)));
                            orderItems.add(item);
                        }

                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("userId", userId);
                        orderData.put("items", orderItems);
                        orderData.put("timestamp", com.google.firebase.Timestamp.now()); // Use Firebase Timestamp

                        //save order in Firestore
                        db.collection("orders").add(orderData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("CartActivity", "Order placed successfully: " + documentReference.getId());

                                    //clear cart
                                    db.collection("users").document(userId).update("cart", new ArrayList<>())
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("CartActivity", "Cart cleared successfully.");
                                                productNames.clear();
                                                productPrices.clear();
                                                itemCounts.clear();
                                                adapter.notifyDataSetChanged();
                                                updateTotalPrice();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("CartActivity", "Error clearing cart: ", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CartActivity", "Error placing order: ", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartActivity", "Error querying Firestore: ", e);
                });
    }

    //handles quantity increase for products
    @Override
    public void onIncreaseQuantity(String productName) {
        itemCounts.put(productName, itemCounts.get(productName) + 1);
        updateDatabase();
        updateTotalPrice();
    }
    //handles quantity decrease for products
    @Override
    public void onDecreaseQuantity(String productName) {
        int quantity = itemCounts.get(productName) - 1;
        if (quantity <= 0) {
            //remove the item locally and from Firestore
            itemCounts.remove(productName);
            int index = productNames.indexOf(productName);
            if (index != -1) {
                productNames.remove(index);
                productPrices.remove(index);
                adapter.notifyItemRemoved(index);
                adapter.notifyItemRangeChanged(index, productNames.size());
            }
        } else {
            //update quantity locally
            itemCounts.put(productName, quantity);
        }
        updateDatabase();
        updateTotalPrice();
    }
    //updates the database on quantity increase and decrease.
    private void updateDatabase() {
        String userEmail = m_auth.getCurrentUser().getEmail();

        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String userId = querySnapshot.getDocuments().get(0).getId();

                        //create updated cart data
                        List<Map<String, Object>> updatedCart = new ArrayList<>();
                        for (int i = 0; i < productNames.size(); i++) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", productNames.get(i));
                            item.put("price", productPrices.get(i));
                            item.put("quantity", itemCounts.get(productNames.get(i)));
                            updatedCart.add(item);
                        }

                        //update firestore
                        db.collection("users").document(userId).update("cart", updatedCart)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("CartActivity", "Cart updated successfully.");
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CartActivity", "Error updating cart: ", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartActivity", "Error querying Firestore: ", e);
                });
    }

    private void setTheme(boolean toggle_state) {
        if (toggle_state) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void loadTheme() {
        boolean toggle_theme = getSharedPreferences("Settings", MODE_PRIVATE)
                .getBoolean("My_State", true);
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

}
