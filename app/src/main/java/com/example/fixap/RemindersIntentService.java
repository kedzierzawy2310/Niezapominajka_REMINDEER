package com.example.fixap;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;

import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.availableBeacons;
import static com.example.fixap.BeaconApplication.background;
import static com.example.fixap.BeaconApplication.reminderList;
import static com.example.fixap.MainActivity.activityFunctions;

public class RemindersIntentService extends IntentService {

    public static boolean changed = false;

    public RemindersIntentService() {
        super("RemindersIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        activityFunctions.restoreReminders(this);
        activityFunctions.restoreBeacons(this);
        activityFunctions.restoreUUID(this);

        final Intent beaconService = new Intent(this,
                MyBeaconService.class);
        final Calendar calendar = Calendar.getInstance();
        boolean empty = false;
        for(int i = 0; i<reminderList.size(); i++) {
            Reminder reminder = reminderList.get(i);
            if (!reminder.isWorkType()) {
                calendar.set(reminder.getYearStart(),
                        reminder.getMonthStart()-1,
                        reminder.getDayStart(),
                        reminder.getHourStart(),
                        reminder.getMinuteStart());
                if(System.currentTimeMillis()
                        >= calendar.getTimeInMillis()){
                    calendar.set(reminder.getYearEnd(),
                            reminder.getMonthEnd()-1,
                            reminder.getDayEnd(),
                            reminder.getHourEnd(),
                            reminder.getMinuteEnd());
                    if(System.currentTimeMillis() < calendar.getTimeInMillis()) {
                        if (!reminder.isAlertActive()
                                && !reminder.isWorkType() && !reminder.isWasAutoActivated()) {
                            reminder.setWasAutoActivated(true);
                            reminder.setAlertActive(true);
                            if(activeReminderList.isEmpty())
                                empty = true;
                            activeReminderList.add(reminder);
                            activityFunctions
                                    .resetReminders(getApplicationContext());
                            if(background) makeNotification(reminder, true);
                            changed = true;
                            if(!empty) {
                                stopService(beaconService);
                                availableBeacons.clear();
                            }
                            startService(beaconService);
                            Intent broadcastIntent = new Intent(this,
                                    RemindersBroadcastReceiver.class);
                            sendBroadcast(broadcastIntent);
                            PendingIntent pendingIntent = null;
                            if (android.os.Build.VERSION.SDK_INT
                                    >= android.os.Build.VERSION_CODES.M) {
                                pendingIntent = PendingIntent
                                        .getBroadcast(this, 0,
                                                broadcastIntent, 0);
                            }
                            AlarmManager alarmManager = (AlarmManager)
                                    getSystemService(Context
                                            .ALARM_SERVICE);
                            alarmManager.set(AlarmManager.RTC_WAKEUP,
                                    calendar.getTimeInMillis(),
                                    pendingIntent);
                        }
                    }
                }
                calendar.set(reminder.getYearEnd(),
                        reminder.getMonthEnd()-1,
                        reminder.getDayEnd(),
                        reminder.getHourEnd(),
                        reminder.getMinuteEnd());
                if (System.currentTimeMillis() >= calendar.getTimeInMillis()) {
                    if(reminder.isAlertActive())
                        activeReminderList.remove(reminder);
                    reminderList.remove(reminder);
                    if(activeReminderList.isEmpty()) {
                        stopService(beaconService);
                        availableBeacons.clear();
                    }
                    activityFunctions.resetReminders(getApplicationContext());
                    if(background) makeNotification(reminder, false);
                    changed = true;
                }
            }
        }
    }

    private void makeNotification(Reminder reminder, boolean activate){
        String title;
        String text;
        int color;
        int icon;
        Intent newIntent = new Intent(this, MainActivity.class);

        if(activate){
            title = "ITEM: " + reminder.getType() + " - HAS BEEN ACTIVATED";
            text = "Tap to open ReminDEER";
            color = Color.WHITE;
            icon = android.R.drawable.ic_menu_add;
        }
        else {
            title = "ITEM: " + reminder.getType() + " - HAS BEEN DEACTIVATED";
            text = "Tap to open ReminDEER";
            color = Color.WHITE;
            icon = android.R.drawable.ic_menu_delete;
        }
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        builder.setAutoCancel(true);
        builder.setVibrate(new long[] { 1000, 1000 });
        builder.setLights(color, 3000, 3000);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        int ACTIVATE_NOTIFICATION_ID = 200;
        managerCompat.notify(ACTIVATE_NOTIFICATION_ID + reminder.getId(), notification);
    }
}
