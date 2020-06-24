package com.example.fixap;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationIntentService extends IntentService {

    private static final int NOTIFICATION_ID = 5;

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Are you going out today?");
        builder.setContentText("Tap to not forget about your items");
        builder.setSmallIcon(R.drawable.logo_notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        builder.setAutoCancel(true);
        builder.setVibrate(new long[] { 1000, 1000 });
        builder.setLights(Color.BLUE, 3000, 3000);
        ActivityFunctions activityFunctions = intent.getParcelableExtra("activityFunctions");
        Intent newIntent = new Intent(this, ChooseReminder.class);
        newIntent.putExtra("activityFunctions", (Parcelable) activityFunctions);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notification);
    }
}
