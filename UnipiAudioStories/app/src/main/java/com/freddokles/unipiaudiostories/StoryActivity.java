package com.freddokles.unipiaudiostories;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoryActivity extends AppCompatActivity {

    String story,story_name,story_image;
    long storyId;
    private TextToSpeech tts;
    private boolean isTtsReady = false;
    private static final String PREF_NAME = "StoryPrefs";
    private static final String STORY_LIST = "StoryList";
    private Gson gson;

    private SharedPreferences sharedPreferences;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent(); //get intent that started this activity
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); //initialize shared preferences
        gson = new Gson(); //initialize gson for json handling
        loadLocale(); //load the stored language preference

        List<Map<String, Object>> stories = loadStories(); //load saved stories
        //check if story list is empty, if so, add default stories
        if (stories.isEmpty()) {
            addStory(stories, "The Star Who Lost Its Sparkle", 0);
            addStory(stories, "Benny the Brave Bunny", 0);
            addStory(stories, "The Snail Who Wanted to Race", 0);
            addStory(stories, "The Magic Paintbrush", 0);
            addStory(stories, "Leo and the Whispering Wind", 0);
            saveStories(stories); //save the stories list
        }

        storyId = intent.getIntExtra("storyId",1); //retrieve the story id from the intent

        db = FirebaseFirestore.getInstance(); //initialize firestore database
        fetchStories(); //fetch story data from firestore
    }

    //method to add a story to the list
    private void addStory(List<Map<String, Object>> stories, String name, int reads) {
        Map<String, Object> story = new HashMap<>();
        story.put("name", name);
        story.put("reads", reads);
        stories.add(story);
    }

    //method to save the list of stories to shared preferences
    private void saveStories(List<Map<String, Object>> stories) {
        String json = gson.toJson(stories);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(STORY_LIST, json);
        editor.apply(); //commit changes asynchronously
    }

    //method to load the list of stories from shared preferences
    private List<Map<String, Object>> loadStories() {
        String json = sharedPreferences.getString(STORY_LIST, null);
        if (json == null) {
            return new ArrayList<>(); //return an empty list if no data is found
        }
        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(json, type);
    }

    //method to update read count of a story
    private void updateReadCount(){
        List<Map<String, Object>> stories = loadStories();
        for (Map<String, Object> story : stories){
            if (story_name.equals(story.get("name"))){
                double currentReads = (double) story.get("reads");
                story.put("reads",currentReads+1);
                break;
            }
        }
        saveStories(stories);
        Log.e("Analytics", stories.toString());
    }

    //method to handle text-to-speech functionality
    private void handleTTS(){
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Log.i("TTS", "Initialization Success");
                tts.setLanguage(Locale.UK);
                tts.setSpeechRate(0.9f);
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });

        //initialize button to trigger text-to-speech
        findViewById(R.id.ttsButton).setOnClickListener(v -> {
            if (!tts.isSpeaking()) {
                speakText(story);
                updateReadCount(); //increment read count when story is read aloud
            }else{
                tts.stop(); //stop tts if already speaking
            }
        });
    }

    //method to speak the given text using text-to-speech
    private void speakText(String text) {
        if (tts != null && text != null && !text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    //method to fetch story data from firestore
    private void fetchStories() {
        db.collection("Stories")
                .whereEqualTo("story_id", storyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            story = document.getString("story_string");
                            story_name = document.getString("story_name");
                            story_image = document.getString("story_image");

                            if (story != null && story_name != null && story_image != null) {
                                setStory(story,story_name,story_image);
                                handleTTS();
                            }
                        }
                    } else {
                        Log.e("fetchStories", "Error fetching stories", task.getException());
                    }
                });
    }

    //method to update ui elements with story details
    private void setStory(String story, String storyName, String storyImage) {
        TextView story_body = findViewById(R.id.story);
        TextView story_title = findViewById(R.id.story_title);
        story_body.setText(story);
        story_title.setText(storyName);
        ImageView story_image = findViewById(R.id.story_image);
        ImageView background = findViewById(R.id.background_image);
        int drawable = getResources().getIdentifier(storyImage, "drawable", getPackageName());
        story_image.setImageResource(drawable);
        background.setImageResource(drawable);
    }
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

            recreate();
        }
    }

    private void loadLocale() {
        String languageCode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", "en"); // Default to English
        setLocale(languageCode);
        Log.e("languageCode:",languageCode );
    }
}
