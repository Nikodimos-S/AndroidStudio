package com.freddokles.unipiplishopping;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth m_auth;
    FirebaseFirestore db;
    private SwitchCompat theme_toggle;
    private SwitchCompat geofence_toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        m_auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        theme_toggle = findViewById(R.id.theme_toggle);
        geofence_toggle = findViewById(R.id.geofence_toggle);
        setupSwitch();
        loadTheme();
        loadLocale();
        setupGeofenceToggle();
        loadGeofenceToggleState();

        //navbar
        ImageView homeBtn = findViewById(R.id.homeButton);
        ImageView cartBtn = findViewById(R.id.cartButton);

        homeBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            startActivity(intent);
        });
        cartBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, CartActivity.class);
            startActivity(intent);
        });


        //gets user first and last name, if that doesn't exist returns username
        TextView test = findViewById(R.id.textView);
        fetchUserDocumentByEmail(m_auth.getCurrentUser().getEmail(), new OnUserFetchedCallback() {
            @Override
            public void onUserFetched(String fullName) {
                test.setText(fullName);
            }
        });


        //set up locale spinner menu

        Spinner languageSpinner = findViewById(R.id.language_spinner);

        String currentLanguage = Locale.getDefault().getLanguage();
        if (currentLanguage.equals("el")) {
            languageSpinner.setSelection(1);
        } else if (currentLanguage.equals("zh")) {
            languageSpinner.setSelection(2);
        } else {
            languageSpinner.setSelection(0);
        }

        Button signOutButton = findViewById(R.id.buy_btn_);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        // Handle selection changes
        languageSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) {
                    setLocale("en");
                } else if (position == 1) {
                    setLocale("el");
                } else if (position == 2) {
                    setLocale("zh");
                }
            }


            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });



    }
    //callback interface to pass back name value
    public interface OnUserFetchedCallback {
        void onUserFetched(String fullName);
    }
    //configures geofencing toggle listener
    private void setupGeofenceToggle() {
        geofence_toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("Settings", MODE_PRIVATE)
                    .edit()
                    .putBoolean("Geofence_Enabled", isChecked)
                    .apply();
            Log.d("GeofenceToggle", "Geofence enabled: " + isChecked);
        });
    }
    //loads state from shared preferences
    private void loadGeofenceToggleState() {
        boolean isGeofenceEnabled = getSharedPreferences("Settings", MODE_PRIVATE)
                .getBoolean("Geofence_Enabled", false); // Default to false
        geofence_toggle.setChecked(isGeofenceEnabled);
    }
    //takes email and returns user document from firestore
    private void fetchUserDocumentByEmail(String email, OnUserFetchedCallback callback) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String firstName = document.getString("firstname");
                            String lastName = document.getString("lastname");
                            String username = document.getString("username");

                            if (firstName != null && lastName != null) {
                                String fullName = firstName + " " + lastName;
                                callback.onUserFetched(fullName);
                            } else {
                                Log.d("UserDocument", "'firstname' or 'lastname' field not found in the document. Using username instead.");
                                callback.onUserFetched(username);
                            }
                        }
                    } else {
                        Log.w("UserDocument", "Error getting documents.", task.getException());
                    }
                });
    }
    //sets up theme toggle
    private void setupSwitch() {
        theme_toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("Settings", MODE_PRIVATE)
                    .edit()
                    .putBoolean("My_State", isChecked)
                    .apply();
            setTheme(isChecked);
            Log.d("MyApp", "isEnabled: " + isChecked);
        });
    }
    //sets the theme according to toggle state
    private void setTheme(boolean toggle_state) {

        if (toggle_state) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    //loads theme from shared prefrences, defaults to dark mode
    private void loadTheme() {
        boolean toggle_theme = getSharedPreferences("Settings", MODE_PRIVATE)
                .getBoolean("My_State", true);
        setTheme(toggle_theme);
        theme_toggle.setChecked(toggle_theme);
    }
    //sets locale if the current locale is different
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale currentLocale = Locale.getDefault();
        Log.e("locale:",""+locale );
        Log.e("currentLocale:",""+currentLocale );

        if (!currentLocale.getLanguage().equals(languageCode)) {
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());

            saveLocale(languageCode);

            recreate();
        }
    }
    //saves locale to shared preferences
    private void saveLocale(String languageCode) {
        getSharedPreferences("Settings", MODE_PRIVATE)
                .edit()
                .putString("My_Lang", languageCode)
                .apply();
    }
    //load locale from shared preferences
    private void loadLocale() {
        String languageCode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", "en"); // Default to English
        setLocale(languageCode);
        Log.e("languageCode:",languageCode );
    }
    //used to sign out
    private void signOut() {
        m_auth.signOut();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }



}
