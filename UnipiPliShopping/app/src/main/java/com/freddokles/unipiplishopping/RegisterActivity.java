package com.freddokles.unipiplishopping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        loadTheme();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Firebase session management
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }



        Button loginBtn = findViewById(R.id.gotoLoginBtn);
        Button rgstrBtn = findViewById(R.id.gotoRegisterBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        String email = findViewById(R.id.registeremailField).toString();
        String password = findViewById(R.id.registerpasswordField).toString();
        rgstrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Retrieve email and password from input fields
                String email = ((EditText) findViewById(R.id.registeremailField)).getText().toString().trim();
                String password = ((EditText) findViewById(R.id.registerpasswordField)).getText().toString().trim();
                String username = ((EditText) findViewById(R.id.registernameField)).getText().toString().trim();
                String fname = ((EditText) findViewById(R.id.registerfirstnameField)).getText().toString().trim();
                String lname = ((EditText) findViewById(R.id.registerlastnameField)).getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Registration successful
                                    Map<String, Object> db_user = new HashMap<>();
                                    db_user.put("email", email);
                                    db_user.put("firstname", fname);
                                    db_user.put("lastname", lname);
                                    db_user.put("username", username);
                                    db_user.put("cart", new ArrayList<>());
                                    db_user.put("date_joined", new Timestamp(new Date()));

                                    db.collection("users")
                                            .add(db_user)
                                    ;

                                    Toast.makeText(RegisterActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    // Call a method to update UI or proceed
                                    updateUI(user);
                                } else {
                                    // Registration failed
                                    Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });
            }
        });

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

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Proceed to the next activity or update the current UI
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Handle null case if needed
        }
    }

}