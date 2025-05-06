package com.example.wordy.activity;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.TempPref.PrefsHelper;
import com.example.wordy.model.User;
import com.example.wordy.utils.ReminderBroadcast;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout, phoneLayout, birthdayLayout, emailLayout, passwordLayout;
    private EditText usernameInput, phoneInput, birthdayInput, emailInput, passwordInput;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PrefsHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Authentication and Firestore
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

        Button btnSignup = findViewById(R.id.signup_button);
        Button btnLoginText = findViewById(R.id.login_text);

        // Handle birthday input click
        birthdayInput.setOnClickListener(v -> {
            String currentDateString = birthdayInput.getText().toString().trim();
            long selectedDateMillis;

            if (!currentDateString.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    Date selectedDate = dateFormat.parse(currentDateString);
                    assert selectedDate != null;
                    selectedDateMillis = selectedDate.getTime();
                } catch (ParseException e) {
                    selectedDateMillis = MaterialDatePicker.todayInUtcMilliseconds();
                }
            } else {
                selectedDateMillis = MaterialDatePicker.todayInUtcMilliseconds();
            }

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select birthday")
                    .setSelection(selectedDateMillis)
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateString = dateFormat.format(new Date(selection));
                birthdayInput.setText(dateString);
                birthdayLayout.setHelperText(dateString);
            });
        });

        // Text change listeners for input validation
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                usernameLayout.setError(s.length() > 25 ? "Too long*" : "");
            }
        });

        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                phoneLayout.setError(s.length() == 0 ? "Required*" : "");
            }
        });

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                emailLayout.setError(s.length() == 0 ? "Required*" : "");
            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                passwordLayout.setError(s.length() == 0 ? "Required*" : "");
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
        email = email.contains("@") ? email : email.isEmpty() ? email : email + emailLayout.getSuffixText();
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
                            User user = new User(username, birthday, phone);

                            db.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        prefs.setLoggedIn(true);
                                        prefs.setUserId(firebaseUser.getUid());
                                        Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                        showPickTimeDialog();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignupActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Signup failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Signup failed: " + task.getException().getMessage());
                    }
                });
    }

    private void showPickTimeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pick_reminder_time);

        Button btnPickTime = dialog.findViewById(R.id.btnPickTime);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnSkip = dialog.findViewById(R.id.btnSkip);
        TextView txtTimeReminder = dialog.findViewById(R.id.txtTimeReminder);

        final int[] selectedHour = {20};
        final int[] selectedMinute = {0};

        btnPickTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (TimePicker view, int hourOfDay, int minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        txtTimeReminder.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        txtTimeReminder.setVisibility(TextView.VISIBLE);
                    },
                    selectedHour[0],
                    selectedMinute[0],
                    true
            );
            timePickerDialog.show();
        });

        btnConfirm.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("ReminderPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("notify_hour", selectedHour[0]);
            editor.putInt("notify_minute", selectedMinute[0]);
            editor.putBoolean("notify_enabled", true);
            editor.apply();

            scheduleDailyReminder();

            dialog.dismiss();
            finish();
        });

        btnSkip.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("ReminderPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("notify_hour", 0);
            editor.putInt("notify_minute", 0);
            editor.putBoolean("notify_enabled", false);
            editor.apply();

            dialog.dismiss();
            finish();
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void scheduleDailyReminder() {
        SharedPreferences prefs = getSharedPreferences("ReminderPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("notify_enabled", false)) {
            Log.d("NOTIFY", "Notifications disabled, skipping alarm scheduling");
            return;
        }

        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("NOTIFY", "AlarmManager is null");
            return;
        }

        int hour = prefs.getInt("notify_hour", 20);
        int minute = prefs.getInt("notify_minute", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Log.d("NOTIFY", "Scheduling daily reminder at " + hour + ":" + minute);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    Log.w("NOTIFY", "No permission to schedule exact alarms (SCHEDULE_EXACT_ALARM)");
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(settingsIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            Log.e("NOTIFY", "SCHEDULE_EXACT_ALARM permission denied", e);
            Toast.makeText(this, "Please enable exact alarm permission in settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}