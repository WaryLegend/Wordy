package com.example.wordy.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.R;
import com.example.wordy.utils.ReminderBroadcast;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private TextView tvSelectedTime;
    private SwitchMaterial switchNotification;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        View headerLayout = findViewById(R.id.header);
        headerLayout.findViewById(R.id.header_title).setVisibility(View.GONE);
        headerLayout.findViewById(R.id.btnIconRight).setVisibility(View.GONE);


        Button btnPickTime = findViewById(R.id.btnPickTime);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        switchNotification = findViewById(R.id.switchNotification);

        prefs = getSharedPreferences("ReminderPrefs", MODE_PRIVATE);

        loadCurrentSettings();

        btnPickTime.setOnClickListener(v -> openTimePicker());
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notify_enabled", isChecked).apply();
            if (isChecked) {
                scheduleDailyReminder();
            } else {
                cancelDailyReminder();
            }
        });
        findViewById(R.id.btnReturn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void loadCurrentSettings() {
        int hour = prefs.getInt("notify_hour", 20);
        int minute = prefs.getInt("notify_minute", 0);
        boolean notifyEnabled = prefs.getBoolean("notify_enabled", true);

        tvSelectedTime.setText(String.format(Locale.getDefault(),"%02d:%02d", hour, minute));
        switchNotification.setChecked(notifyEnabled);
    }

    private void openTimePicker() {
        int hour = prefs.getInt("notify_hour", 20);
        int minute = prefs.getInt("notify_minute", 0);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (TimePicker view, int selectedHour, int selectedMinute) -> {
            prefs.edit()
                    .putInt("notify_hour", selectedHour)
                    .putInt("notify_minute", selectedMinute)
                    .apply();

            tvSelectedTime.setText(String.format(Locale.getDefault(),"%02d:%02d", selectedHour, selectedMinute));

            if (switchNotification.isChecked()) {
                scheduleDailyReminder(); // Cập nhật lịch mới
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void scheduleDailyReminder() {
        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            int hour = prefs.getInt("notify_hour", 20);
            int minute = prefs.getInt("notify_minute", 0);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Log.d("NOTIFY", "Đặt lịch nhắc học mỗi ngày lúc " + hour + ":" + minute);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    } else {
                        Log.w("NOTIFY", "App không có quyền đặt lịch chính xác (SCHEDULE_EXACT_ALARM).");
                    }
                }
            } catch (SecurityException e) {
                Log.e("NOTIFY", "Không có quyền SCHEDULE_EXACT_ALARM", e);
            }

        }
    }

    private void cancelDailyReminder() {
        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}