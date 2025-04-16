package com.example.wordy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, email, phone, birthday;
    private Button btnSave,btnReturn,btnLogout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userName = findViewById(R.id.username);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phoneNumber);
        birthday = findViewById(R.id.dateOfBirth);
        btnSave = findViewById(R.id.btnSave);

        btnReturn = findViewById(R.id.btnReturn);
        btnLogout = findViewById(R.id.btnLogout);

        // tiêu đề
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText("Profile");
        // bỏ cái icon bên phải (don't mind here)
        Button rm = headerLayout.findViewById(R.id.btnIconRight);
        rm.setVisibility(View.GONE);

        String userId = new PrefsHelper(this).getUserId();
        userRef = db.collection("users").document(userId);

        loadUserData();

        btnReturn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            new PrefsHelper(this).clear();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        btnSave.setOnClickListener(v -> {
            User updatedUser = new User(
                    userName.getText().toString(),
                    phone.getText().toString(),
                    birthday.getText().toString()
            );

            userRef.set(updatedUser)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

    }

    private void loadUserData() {
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    userName.setText(user.getName());
                    phone.setText(user.getPhone());
                    email.setText(mAuth.getCurrentUser().getEmail());
                    birthday.setText(user.getBirthday());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
