package com.example.wordy.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.User;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout, phoneLayout, birthdayLayout, emailLayout, passwordLayout;
    private EditText usernameInput, phoneInput, birthdayInput, emailInput, passwordInput;
    private Button btnLoginText, btnSignup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PrefsHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo Firebase Authentication và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = new PrefsHelper(this);

        usernameLayout = findViewById(R.id.username_layout);
        phoneLayout = findViewById(R.id.phone_layout);
        birthdayLayout = findViewById(R.id.birthday_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);

        usernameInput = findViewById(R.id.usernameInput);
        phoneInput = findViewById(R.id.phoneInput);
        birthdayInput = findViewById(R.id.birthdayInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        btnSignup = findViewById(R.id.signup_button);
        btnLoginText = findViewById(R.id.login_text);

        // Sự kiện click vào trường ngày sinh
        birthdayInput.setOnClickListener(v -> {
            // Lấy ngày hiện tại trong birthdayInput (nếu có chọn trước đó)
            String currentDateString = birthdayInput.getText().toString().trim();
            Long selectedDateMillis;

            if (!currentDateString.isEmpty()) {
                // Parse ngày đã chọn trước đó
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    Date selectedDate = dateFormat.parse(currentDateString);
                    selectedDateMillis = selectedDate.getTime();
                } catch (ParseException e) {
                    selectedDateMillis = MaterialDatePicker.todayInUtcMilliseconds(); // Mặc định hôm nay nếu parse lỗi
                }
            } else {
                selectedDateMillis = MaterialDatePicker.todayInUtcMilliseconds(); // Mặc định hôm nay nếu chưa chọn
            }

            // Tạo MaterialDatePicker
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày sinh")
                    .setSelection(selectedDateMillis) // Ngày mặc định là hôm nay
                    .build();

            // Hiển thị DatePicker
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            // Xử lý khi người dùng chọn ngày
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateString = dateFormat.format(new Date(selection));
                birthdayInput.setText(dateString);
                birthdayLayout.setHelperText(dateString);
            });
        });

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 25) {
                    usernameLayout.setError("Too long*");
                } else {
                    usernameLayout.setError("");
                }
            }
        });

        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    phoneLayout.setError("");
                }
            }
        });

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    emailLayout.setError("");
                }
            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    passwordLayout.setError("");
                }
            }
        });

        btnSignup.setOnClickListener(v -> signupUser());
        btnLoginText.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void signupUser() {
        String username = usernameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        email = email.contains("@") ? email : email.isEmpty()? email : email + emailLayout.getSuffixText();

        String password = passwordInput.getText().toString().trim();


        if (username.isEmpty()) {
            usernameLayout.setError("Required*");
        } else if (username.length() > 25) {
            usernameLayout.setError("Too long*");
        }

        if (phone.isEmpty()) {
            phoneLayout.setError("Required*");
        }

        if (email.isEmpty()) {
            emailLayout.setError("Required*");
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Required*");
        }

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Tạo đối tượng User
                            User user = new User(username, birthday, phone);

                            // Lưu vào Firestore với UID làm document ID
                            db.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // Lưu trạng thái đăng nhập và userId
                                        prefs.setLoggedIn(true);
                                        prefs.setUserId(firebaseUser.getUid());

                                        Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Signup failed: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}