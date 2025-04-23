package com.example.wordy.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.Topic;
import com.example.wordy.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MaterialCardView btnProfile, btnDictionary, btnTopic;
    private TextView username, progressPercentage;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private PrefsHelper prefs;

    private final int TOTAL_WORDS = 100;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        prefs = new PrefsHelper(this);

        username = findViewById(R.id.username);
        progressBar = findViewById(R.id.progressBar);
        progressPercentage = findViewById(R.id.progress_percentage);


        if (prefs.isLoggedIn()) {
            String uid = prefs.getUserId();
            db.collection("users").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = task.getResult().toObject(User.class);
                            if (user != null) {
                                username.setText(user.getName());
                            }
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            calculateProgress(uid);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnTopic = findViewById(R.id.btnTopic);
        btnTopic.setOnClickListener(v -> startActivity(new Intent(this, TopicActivity.class)));

        btnDictionary = findViewById(R.id.btnDictionary);
        btnDictionary.setOnClickListener(v -> startActivity(new Intent(this, DictionaryActivity.class)));


        findViewById(R.id.cardProgress).setOnClickListener(v -> {
            startActivity(new Intent(this, LearnedWordsActivity.class));
        });
    }

    private void calculateProgress(String userId) {
        List<String> topicIds = loadTopicIdsFromJson();
        if (topicIds == null || topicIds.isEmpty()) return;

        final int[] learnedCount = {0};
        final int totalTopics = topicIds.size();
        final int[] topicsHandled = {0};

        for (String topicId : topicIds) {
            db.collection("learnedWords")
                    .document(userId)
                    .collection(topicId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        learnedCount[0] += snapshot.size();
                        topicsHandled[0]++;

                        if (topicsHandled[0] == totalTopics) {
                            updateProgressUI(learnedCount[0]);
                        }
                    });
        }
    }



    private List<String> loadTopicIdsFromJson() {
        List<String> topicIds = new ArrayList<>();

        try {
            InputStream is = getAssets().open("voca.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonArray vocabularyArray = jsonObject.getAsJsonArray("vocabulary");

            for (int i = 0; i < vocabularyArray.size(); i++) {
                JsonObject topicObject = vocabularyArray.get(i).getAsJsonObject();
                String topicName = topicObject.get("name").getAsString();


                String topicId = topicName.toLowerCase().replace(" ", "_");
                topicIds.add(topicId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return topicIds;
    }

    private void updateProgressUI(int learnedCount) {
        int totalWords = 100;
        int progress = (int) ((learnedCount / (float) totalWords) * 100);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView progressText = findViewById(R.id.progress_percentage);

        progressBar.setProgress(progress);
        progressText.setText(progress + "%");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.isLoggedIn()) {
            String uid = prefs.getUserId();
            calculateProgress(uid);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
