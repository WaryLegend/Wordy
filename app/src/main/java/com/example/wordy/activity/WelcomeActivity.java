package com.example.wordy.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.utils.NotificationHelper;
import com.example.wordy.utils.ReminderBroadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import java.util.Calendar;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        NotificationHelper.showNotification(
                                this,
                                "Wordy Reminder",
                                "Welcome to Wordy!"
                        );
                    }
                });

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            NotificationHelper.showNotification(
                    this,
                    "Wordy Reminder",
                    "Welcome to Wordy!"
            );
        }

        // Schedule daily reminder
        scheduleDailyReminder();

        // Set up Get Started button
        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void scheduleDailyReminder() {
        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the set time is in the past, schedule for next day
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        try {
            // Use setExactAndAllowWhileIdle for better reliability on modern Android
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } catch (SecurityException e) {
            // Handle case where exact alarm permission is not granted
            NotificationHelper.showNotification(
                    this,
                    "Wordy Reminder",
                    "Please enable exact alarm permission in settings"
            );
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the permission launcher to prevent memory leaks
        if (requestPermissionLauncher != null) {
            requestPermissionLauncher.unregister();
        }
    }
}