package com.example.fixap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

public class NotificationReceiver extends BroadcastReceiver {
    public  NotificationReceiver(){}

    public void onReceive(Context context, Intent intent) {
        ActivityFunctions activityFunctions = intent.getParcelableExtra("activityFunctions");
        Intent intent1 = new Intent(context, NotificationIntentService.class);
        intent1.putExtra("activityFunctions", (Parcelable) activityFunctions);
        context.startService(intent1);
    }
}
