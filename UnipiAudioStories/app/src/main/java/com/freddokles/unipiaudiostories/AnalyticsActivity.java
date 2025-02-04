package com.freddokles.unipiaudiostories;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {
    //define shared preferences name and key for storing story list
    private static final String PREF_NAME = "StoryPrefs";
    private static final String STORY_LIST = "StoryList";
    SharedPreferences sharedPreferences;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analytics);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); //initialize shared preferences
        gson = new Gson(); //initialize gson instance for json handling
        loadLocale(); //load previously saved language settings
        List<Map<String, Object>> stories = loadStories(); //retrieve stored stories from shared preferences

        //sort stories based on read count in descending order
        Collections.sort(stories, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                Double value1 = (Double) map1.get("reads");
                Double value2 = (Double) map2.get("reads");
                return value2.compareTo(value1);
            }
        });

        //log sorted stories for debugging
        for (Map<String, Object> map : stories) {
            Log.e("Gamao", map.toString());
        }

        ListView listView = findViewById(R.id.listView); //find listview in layout

        //define data keys and corresponding textview ids for list adapter
        String[] from = {"name", "reads"};
        int[] to = {R.id.text1, R.id.text2};

        //create and set adapter for displaying stories in listview
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                stories,
                R.layout.list_item,
                from,
                to
        );

        listView.setAdapter(adapter);
        Spinner languageSpinner = findViewById(R.id.language_spinner); //find spinner for language selection

        //set spinner selection based on current system language
        String currentLanguage = Locale.getDefault().getLanguage();
        if (currentLanguage.equals("el")) {
            languageSpinner.setSelection(2);
        } else if (currentLanguage.equals("zh")) {
            languageSpinner.setSelection(1);
        } else {
            languageSpinner.setSelection(0);
        }

        //handle language selection changes
        languageSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) {
                    setLocale("en");
                } else if (position == 2) {
                    setLocale("el");
                } else if (position == 1) {
                    setLocale("zh");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    //method to load stored stories from shared preferences
    private List<Map<String, Object>> loadStories() {
        String json = sharedPreferences.getString(STORY_LIST, null);
        if (json == null) {
            return new ArrayList<>(); //return empty list if no data is found
        }
        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(json, type);
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

            saveLocale(languageCode); //save new language preference
            recreate(); //restart activity to apply changes
        }
    }

    //method to save selected language to shared preferences
    private void saveLocale(String languageCode) {
        getSharedPreferences("Settings", MODE_PRIVATE)
                .edit()
                .putString("My_Lang", languageCode)
                .apply();
    }

    //method to load saved language preference and apply it
    private void loadLocale() {
        String languageCode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", "en"); //default to english
        setLocale(languageCode);
        Log.e("languageCode:",languageCode );
    }
}
