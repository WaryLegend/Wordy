package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
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

public class HomeActivity extends AppCompatActivity {
    private MaterialCardView btnProfile, btnDictionary, btnTopic;
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
        btnTopic = findViewById(R.id.btnTopic);
        btnDictionary = findViewById(R.id.btnDictionary);

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

        // Button listeners
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            profileResultLauncher.launch(intent);
        });

        btnTopic.setOnClickListener(v -> {
            Intent intent = new Intent(this, TopicActivity.class);
            startActivity(intent);
        });

        btnDictionary.setOnClickListener(v -> {
            Intent intent = new Intent(this, DictionaryActivity.class);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}