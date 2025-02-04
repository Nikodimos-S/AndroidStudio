package com.freddokles.unipiaudiostories;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //variable to store current language
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadLocale(); //load saved locale settings
        currentLanguage = Locale.getDefault().getLanguage(); //store current language

        //initialize story buttons
        LinearLayout story_btn_1 = findViewById(R.id.story1);
        LinearLayout story_btn_2 = findViewById(R.id.story2);
        LinearLayout story_btn_3 = findViewById(R.id.story3);
        LinearLayout story_btn_4 = findViewById(R.id.story4);
        LinearLayout story_btn_5 = findViewById(R.id.story5);
        Button button = findViewById(R.id.button);

        //set click listeners to navigate to respective story activity
        story_btn_1.setOnClickListener(view->GoToStory(1));
        story_btn_2.setOnClickListener(view->GoToStory(2));
        story_btn_3.setOnClickListener(view->GoToStory(3));
        story_btn_4.setOnClickListener(view->GoToStory(4));
        story_btn_5.setOnClickListener(view->GoToStory(5));

        //navigate to analytics activity when button is clicked
        button.setOnClickListener(view->{
            Intent intent = new Intent(MainActivity.this, AnalyticsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String newLanguage = Locale.getDefault().getLanguage();
        //check if language has changed and recreate activity if needed
        //for handling the case where the user changes language and navigates back to the main activity
        if (!newLanguage.equals(currentLanguage)) {
            currentLanguage = newLanguage;
            recreate();
        }
    }

    //method to navigate to the selected story
    private void GoToStory(int story){
        Intent intent = new Intent(MainActivity.this, StoryActivity.class);
        intent.putExtra("storyId",story);
        startActivity(intent);
    }

    //method to change app locale and update configuration
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

            //saveLocale(languageCode);

            recreate(); //restart activity to apply changes
        }
    }

    //method to load saved language preference and apply it
    private void loadLocale() {
        String languageCode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", "en"); //default to english
        setLocale(languageCode);
        Log.e("languageCode:",languageCode );
    }
}
