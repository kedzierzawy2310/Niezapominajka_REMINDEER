package com.example.fixap;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.NotificationManagerCompat;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.util.ArrayList;
import java.util.List;

import static com.example.fixap.MainActivity.activityFunctions;

public class BeaconApplication extends Application {

    final public static List<BeaconRegion> reminderBeacons = new ArrayList<>();
    final public static List<BeaconRegion> availableBeacons = new ArrayList<>();
    final public static List<BeaconRegion> rangedBeaconRegions = new ArrayList<>();
    final public static List<Reminder> reminderList = new ArrayList<>();
    final public static List<Reminder> activeReminderList = new ArrayList<>();
    final public static List<String> UUIDList = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static AvailableBeaconsArrayAdapter adapter;
    public static String UUID; //"B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    public static boolean background = true;
    public static boolean mainDestroyed = true;

    @SuppressLint("ShortAlarm")
    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.cancelAll();

        adapter = new AvailableBeaconsArrayAdapter(this, availableBeacons);
        activityFunctions.restoreReminders(this);
        activityFunctions.restoreBeacons(this);
        activityFunctions.restoreUUID(this);
        availableBeacons.clear();

        if(!activeReminderList.isEmpty()) startService(new Intent(this, MyBeaconService.class));

        Intent notifyIntent = new Intent(this, NotificationReceiver.class);
        notifyIntent.putExtra("activityFunctions", (Parcelable) activityFunctions);
        activityFunctions.SendBroadcastToService(this, notifyIntent, System.currentTimeMillis(), 1000, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
