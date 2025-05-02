package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeActivity extends AppCompatActivity {
    private MaterialCardView btnProfile, btnDictionary, btnTopic, btnMiniGame;
    private TextView username;
    private FirebaseFirestore db;
    private PrefsHelper prefs;
    private ActivityResultLauncher<Intent> profileResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        prefs = new PrefsHelper(this);
        username = findViewById(R.id.username);

        // Check if not logged in
        if (!prefs.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        // Load user data
        loadUserData();

        // Set up buttons
        btnProfile = findViewById(R.id.btnProfile);
        btnDictionary = findViewById(R.id.btnDictionary);
        btnTopic = findViewById(R.id.btnTopic);
        btnMiniGame = findViewById(R.id.btnMiniGame);

        // Set up result launcher for ProfileActivity
        profileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getBooleanExtra("needs_refresh", false)) {
                            loadUserData(); // Reload data if changes were made
                        }
                    }
                }
        );
        // Process bar listener
        findViewById(R.id.cardProgress).setOnClickListener(v -> {
            startActivity(new Intent(this, LearnedWordsActivity.class));
        });

        // Button listeners
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            profileResultLauncher.launch(intent);
        });

        btnDictionary.setOnClickListener(v -> {
            Intent intent = new Intent(this, DictionaryActivity.class);
            startActivity(intent);
        });

        btnTopic.setOnClickListener(v -> {
            Intent intent = new Intent(this, TopicActivity.class);
            startActivity(intent);
        });

        btnMiniGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchingGameActivity.class);
            startActivity(intent);
        });

    }

    private void loadUserData() {
        String uid = prefs.getUserId();
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                username.setText(user.getName());
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        calculateProgress(uid);
    }

    private void calculateProgress(String userId) {
        List<String> topicIds = loadTopicIdsFromJson();
        if (topicIds.isEmpty()) return;

        AtomicInteger learnedCount = new AtomicInteger();
        int totalTopics = topicIds.size();
        int[] topicsHandled = {0};

        for (String topicId : topicIds) {
            db.collection("learnedWords")
                    .document(userId)
                    .collection(topicId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        learnedCount.addAndGet(snapshot.size());
                        topicsHandled[0]++;

                        if (topicsHandled[0] == totalTopics) {
                            updateProgressUI(learnedCount.get());
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
        int totalWords = getTotalWordsFromJson(); // Lấy tổng số từ từ voca.json
        int progress = totalWords > 0 ? (int) ((learnedCount / (float) totalWords) * 100) : 0;

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView progressPercentage = findViewById(R.id.progress_percentage);

        progressBar.setProgress(progress);
        String progressText = String.format(Locale.UK,"%d%%", progress);
        progressPercentage.setText(progressText);
    }

    private int getTotalWordsFromJson() {
        int totalWords = 0;
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
                JsonArray wordsArray = topicObject.getAsJsonArray("words");
                totalWords += wordsArray.size(); // Cộng dồn số từ trong mỗi topic
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalWords;
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