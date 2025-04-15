package com.example.wordy.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class HomeActivity extends AppCompatActivity {
    private MaterialCardView btnProfile,btnDictionary,btnTopic;

    private TextView username;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PrefsHelper prefs;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = new PrefsHelper(this);

        username = findViewById(R.id.username);

        // Kiểm tra xem đã đăng nhập chưa
        if (prefs.isLoggedIn()) {
            // Lấy thông tin người dùng từ Firestore
            String uid = prefs.getUserId();
            db.collection("users").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                username.setText(user.getName());
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Nếu chưa đăng nhập, chuyển về LoginActivity
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        btnTopic = findViewById(R.id.btnTopic);
        btnTopic.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TopicActivity.class);
            startActivity(intent);
        });
        btnDictionary = findViewById(R.id.btnDictionary);
        btnDictionary.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DictionaryActivity.class);
            startActivity(intent);
        });
    }
}
