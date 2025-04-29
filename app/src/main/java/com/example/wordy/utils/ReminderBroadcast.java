package com.example.wordy.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NOTIFY", "Đã nhận Broadcast và chuẩn bị gửi Notification.");

        // Đảm bảo luôn có Notification Channel
        NotificationHelper.createNotificationChannel(context);

        // Gửi Notification nhắc học
        NotificationHelper.showNotification(
                context,
                "Wordy Reminder",
                "Don't forget to learn new words today! 📚✨"
        );
    }
}
