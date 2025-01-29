package com.freddokles.unipiplishopping;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100; // Request code for sign-in
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        loadLocale();
        loadTheme();

        // Configure Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //Default sign-in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // Add your web client ID here
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Register button
        Button registerBtn = findViewById(R.id.gotoRegisterBtn);
        Button loginBtn = findViewById(R.id.loginBtn);
        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(view -> {
            String email = ((EditText) findViewById(R.id.loginusernameField)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.loginpasswordField)).getText().toString().trim();


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(LoginActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        });

        // Google Sign-In button
        ImageView googleSignInBtn = findViewById(R.id.googleBtn); // Update with your actual button ID
        googleSignInBtn.setOnClickListener(view -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                Log.d("LoginActivity", "Google sign-in successful");
                Toast.makeText(this, "Google sign-in successful", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("LoginActivity", "Google sign-in failed", e);
                Toast.makeText(this, "Google sign-in unsuccessful", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Query Firestore to check if the user already exists
                        db.collection("users")
                                .whereEqualTo("email", user.getEmail())
                                .get()
                                .addOnCompleteListener(queryTask -> {
                                    if (queryTask.isSuccessful()) {
                                        QuerySnapshot querySnapshot = queryTask.getResult();

                                        if (querySnapshot.isEmpty()) {
                                            // User does not exist, create a new user document
                                            Map<String, Object> db_user = new HashMap<>();
                                            db_user.put("email", user.getEmail());
                                            db_user.put("username", user.getDisplayName());
                                            db_user.put("cart", new ArrayList<>());
                                            db_user.put("date_joined", new Timestamp(new Date()));

                                            // Add the new user to Firestore
                                            db.collection("users")
                                                    .add(db_user)
                                                    .addOnSuccessListener(documentReference -> {
                                                        Log.d("LoginActivity", "User added to Firestore: " + documentReference.getId());
                                                        // Proceed to the next activity
                                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w("LoginActivity", "Error adding user", e);
                                                    });
                                        } else {
                                            // User already exists
                                            Log.d("LoginActivity", "Firebase sign-in successful: " + user.getDisplayName());
                                            Toast.makeText(this, user.getDisplayName() + " sign-in successful", Toast.LENGTH_SHORT).show();

                                            // Navigate to the next activity
                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Log.w("LoginActivity", "Error checking user existence", queryTask.getException());
                                    }
                                });
                    } else {
                        Log.w("LoginActivity", "Firebase sign-in failed", task.getException());
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Proceed to the next activity or update the current UI
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Handle null case if needed
        }
    }
    private void loadLocale() {
        String languageCode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", "en"); // Default to English
        setLocale(languageCode);
    }
    private void saveLocale(String languageCode) {
        getSharedPreferences("Settings", MODE_PRIVATE)
                .edit()
                .putString("My_Lang", languageCode)
                .apply();
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
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale currentLocale = Locale.getDefault();

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
}
