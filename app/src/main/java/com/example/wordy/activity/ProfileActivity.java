package com.example.wordy.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, email, phone, birthday;
    private Button btnReturn, btnLogout, nameEditor, emailEditor, phoneEditor;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference userRef;
    private boolean dataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        userName = findViewById(R.id.username);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phoneNumber);
        birthday = findViewById(R.id.dateOfBirth);
        btnReturn = findViewById(R.id.btnReturn);
        btnLogout = findViewById(R.id.btnLogout);
        nameEditor = findViewById(R.id.name_editor);
        emailEditor = findViewById(R.id.email_editor);
        phoneEditor = findViewById(R.id.phone_editor);

        // Set up header
        View headerLayout = findViewById(R.id.header);
        TextView headerTitle = headerLayout.findViewById(R.id.header_title);
        headerTitle.setText("Profile");
        Button btnIconRight = headerLayout.findViewById(R.id.btnIconRight);
        if (btnIconRight != null) {
            btnIconRight.setVisibility(View.GONE);
        }

        // Load user data
        String userId = new PrefsHelper(this).getUserId();
        userRef = db.collection("users").document(userId);
        loadUserData();

        // Set up button listeners
        btnReturn.setOnClickListener(v -> {
            if (dataChanged) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("needs_refresh", true);
                setResult(RESULT_OK, resultIntent);
            } else {
                setResult(RESULT_CANCELED);
            }
            super.finish();
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Editor button listeners
        nameEditor.setOnClickListener(v -> showEditDialog("username", "Change Username", userName.getText().toString(), "textPersonName"));
        emailEditor.setOnClickListener(v -> showEditDialog("email", "Change Email", email.getText().toString(), "textEmailAddress"));
        phoneEditor.setOnClickListener(v -> showEditDialog("phone", "Change Phone Number", phone.getText().toString(), "phone"));
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
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void showEditDialog(String field, String title, String currentValue, String inputType) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.edit_field_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setDimAmount(0.5f);
        dialog.setCancelable(true); // Dismiss when clicking outside

        // Set dialog width to nearly match screen width
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        params.width = (int) (metrics.widthPixels * 0.9); // 90% of screen width
        dialog.getWindow().setAttributes(params);

        // Initialize dialog views
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextInputLayout inputLayout = dialog.findViewById(R.id.input_layout);
        TextInputEditText inputField = dialog.findViewById(R.id.input_field);
        Button saveButton = dialog.findViewById(R.id.save_button);

        // Configure dialog
        dialogTitle.setText(title);
        inputField.setText(currentValue);
        inputLayout.setHint(title.replace("Change ", ""));

        // Enable character counter for username only
        if (field.equals("username")) {
            inputLayout.setCounterEnabled(true);
            inputLayout.setCounterMaxLength(25);
        } else {
            inputLayout.setCounterEnabled(false);
        }

        // Set input type
        switch (inputType) {
            case "textPersonName":
                inputField.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "textEmailAddress":
                inputField.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case "phone":
                inputField.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
                break;
        }

        // Save button listener
        saveButton.setOnClickListener(v -> {
            String newValue = inputField.getText().toString().trim();
            if (newValue.isEmpty()) {
                inputField.setError("Field cannot be empty");
                return;
            }

            // Validate inputs
            if (field.equals("username") && newValue.length() > 25) {
                inputField.setError("Too long*");
                return;
            }
            if (field.equals("email") && !newValue.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                inputField.setError("Invalid email address");
                return;
            }
            if (field.equals("phone") && !newValue.matches("\\d{10,15}")) {
                inputField.setError("Invalid phone number (10-15 digits)");
                return;
            }

            // Update data
            updateUserData(field, newValue, dialog);
        });

        dialog.show();
    }

    private void updateUserData(String field, String newValue, Dialog dialog) {
        switch (field) {
            case "username":
                userRef.update("name", newValue)
                        .addOnSuccessListener(aVoid -> {
                            userName.setText(newValue);
                            dataChanged = true;
                            Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show();
                        });
                break;
            case "phone":
                userRef.update("phone", newValue)
                        .addOnSuccessListener(aVoid -> {
                            phone.setText(newValue);
                            Toast.makeText(this, "Phone number updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update phone number", Toast.LENGTH_SHORT).show();
                        });
                break;
            case "email":
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser.verifyBeforeUpdateEmail(newValue)
                            .addOnSuccessListener(aVoid -> {
                                email.setText(newValue);
                                Toast.makeText(this, "Verification email sent. Please verify to update.", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                if (e.getMessage().contains("requires recent authentication")) {
                                    Toast.makeText(this, "Please re-login to update email", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showLogoutConfirmation() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Xác Nhận Đăng Xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Có", (d, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .setCancelable(true)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
    }

    private void performLogout() {
        new PrefsHelper(this).clear();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, 300);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}