package com.example.fixap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.example.fixap.AddReminder.REQUEST_LOCATION;
import static com.example.fixap.AddReminder.REQUEST_TAKE_PHOTO;
import static com.example.fixap.AddReminder.currentPhotoPath;
import static com.example.fixap.BeaconApplication.UUIDList;
import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.reminderBeacons;
import static com.example.fixap.BeaconApplication.reminderList;

class ActivityFunctions implements Parcelable, Serializable {

    private WifiManager wifiManager;

    ActivityFunctions(){
    }

    private ActivityFunctions(Parcel in) {
    }

    public static final Creator<ActivityFunctions> CREATOR = new Creator<ActivityFunctions>() {
        @Override
        public ActivityFunctions createFromParcel(Parcel in) {
            return new ActivityFunctions(in);
        }

        @Override
        public ActivityFunctions[] newArray(int size) {
            return new ActivityFunctions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    void hideStatusBar(Context context, AlertDialog alertDialog){
        Window window;
        if(context != null) {
            window = ((Activity) context).getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            window = Objects.requireNonNull(alertDialog.getWindow());
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        ActionBar actionBar = ((Activity) Objects.requireNonNull(context)).getActionBar();
        Objects.requireNonNull(actionBar).hide();
    }

    void saveReminders(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("reminders_number", reminderList.size());
        for(int i=0; i<reminderList.size(); i++){
            Reminder reminder = reminderList.get(i);

            editor.putString("beacon_region_of_reminder"+i, reminder.getBeaconRegion());
            editor.putString("type_of_reminder"+i, reminder.getType());
            editor.putString("other_type_name_of_reminder"+i, reminder.getOtherTypeName());
            editor.putString("image_of_reminder"+i, reminder.getImage());
            editor.putString("location_of_reminder"+i, reminder.getLocation());
            editor.putString("lost_location_of_reminder"+i, reminder.getLostLocation());
            editor.putString("made_date_of_reminder"+i, reminder.getMadeDate());
            editor.putString("start_date_of_reminder"+i, reminder.getStartDate());
            editor.putString("end_date_of_reminder"+i, reminder.getEndDate());
            editor.putInt("year_of_start_reminder"+i, reminder.getYearStart());
            editor.putInt("month_of_start_reminder"+i, reminder.getMonthStart());
            editor.putInt("day_of_start_reminder"+i, reminder.getDayStart());
            editor.putInt("hour_of_start_reminder"+i, reminder.getHourStart());
            editor.putInt("minute_of_start_reminder"+i, reminder.getMinuteStart());
            editor.putInt("year_of_end_reminder"+i, reminder.getYearEnd());
            editor.putInt("month_of_end_reminder"+i, reminder.getMonthEnd());
            editor.putInt("day_of_end_reminder"+i, reminder.getDayEnd());
            editor.putInt("hour_of_end_reminder"+i, reminder.getHourEnd());
            editor.putInt("minute_of_end_reminder"+i, reminder.getMinuteEnd());
            //editor.putInt("index_of_reminder"+i, reminder.getIndex());
            editor.putInt("id_of_reminder"+i, reminder.getId());
            editor.putBoolean("work_type_of_reminder"+i, reminder.isWorkType());
            editor.putBoolean("is_alert_of_reminder_active"+i, reminder.isAlertActive());
            editor.putBoolean("was_reminder_auto_activated"+i, reminder.isWasAutoActivated());
            editor.putBoolean("is_reminder_lost"+i, reminder.isLost());
            editor.putBoolean("is_with_lost_location"+i, reminder.isWithLostLocation());
        }
        editor.apply();
    }

    void saveBeacons(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("beacons_shared_preferences", MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("beacons_number", reminderBeacons.size());
        for(int i = 0; i<reminderBeacons.size(); i++){
            BeaconRegion beaconRegion = reminderBeacons.get(i);

            editor.putString("beacon_identifier"+i, beaconRegion.getIdentifier());
            editor.putString("beacon_UUID"+i, beaconRegion.getProximityUUID().toString());
            editor.putInt("beacon_major_number"+i, beaconRegion.getMajor());
            editor.putInt("beacon_minor_number"+i, beaconRegion.getMinor());
        }
        editor.apply();
    }

    private void saveUUID(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UUID_shared_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("UUID_number", UUIDList.size());
        for (int i = 0; i<UUIDList.size(); i++){
            String UUID = UUIDList.get(i);

            editor.putString("UUID"+i, UUID);
        }
        editor.apply();
    }

    void restoreReminders(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        int numberOfReminders = sharedPreferences.getInt("reminders_number", 0);
        if(numberOfReminders != 0){
            activeReminderList.clear();
            reminderList.clear();
            for(int i=0; i<numberOfReminders; i++){
                String beaconRegion = sharedPreferences.getString("beacon_region_of_reminder"+i, "0");
                String type = sharedPreferences.getString("type_of_reminder"+i, "0");
                String otherType = sharedPreferences.getString("other_type_name_of_reminder"+i, "0");
                String image = sharedPreferences.getString("image_of_reminder"+i, "0");
                String location = sharedPreferences.getString("location_of_reminder"+i, "0");
                String lostLocation = sharedPreferences.getString("lost_location_of_reminder"+i, "0");
                String madeDate = sharedPreferences.getString("made_date_of_reminder"+i, "0");
                String startDate = sharedPreferences.getString("start_date_of_reminder"+i, "0");
                String endDate = sharedPreferences.getString("end_date_of_reminder"+i, "0");
                int yearStart = sharedPreferences.getInt("year_of_start_reminder"+i,0);
                int monthStart = sharedPreferences.getInt("month_of_start_reminder"+i,0);
                int dayStart = sharedPreferences.getInt("day_of_start_reminder"+i,0);
                int hourStart = sharedPreferences.getInt("hour_of_start_reminder"+i,0);
                int minuteStart = sharedPreferences.getInt("minute_of_start_reminder"+i,0);
                int yearEnd = sharedPreferences.getInt("year_of_end_reminder"+i,0);
                int monthEnd = sharedPreferences.getInt("month_of_end_reminder"+i,0);
                int dayEnd = sharedPreferences.getInt("day_of_end_reminder"+i,0);
                int hourEnd = sharedPreferences.getInt("hour_of_end_reminder"+i,0);
                int minuteEnd = sharedPreferences.getInt("minute_of_end_reminder"+i,0);
                //int index = sharedPreferences.getInt("index_of_reminder"+i, -1);
                int id = sharedPreferences.getInt("id_of_reminder"+i, 0);
                boolean workType = sharedPreferences.getBoolean("work_type_of_reminder"+i, false);
                boolean alertActive = sharedPreferences.getBoolean("is_alert_of_reminder_active"+i, false);
                boolean wasAutoActivated = sharedPreferences.getBoolean("was_reminder_auto_activated"+i, false);
                boolean lost = sharedPreferences.getBoolean("is_reminder_lost"+i, false);
                boolean withLostLocation = sharedPreferences.getBoolean("is_with_lost_location"+i, false);

                Reminder reminder = new Reminder(type, id);
                reminder.setOtherTypeName(otherType);
                reminder.setImage(image);
                reminder.setLocation(location);
                reminder.setMadeDate(madeDate);
                reminder.setStartDate(startDate);
                reminder.setEndDate(endDate);
                reminder.setYearStart(yearStart);
                reminder.setMonthStart(monthStart);
                reminder.setDayStart(dayStart);
                reminder.setHourStart(hourStart);
                reminder.setMinuteStart(minuteStart);
                reminder.setYearEnd(yearEnd);
                reminder.setMonthEnd(monthEnd);
                reminder.setDayEnd(dayEnd);
                reminder.setHourEnd(hourEnd);
                reminder.setMinuteEnd(minuteEnd);
                reminder.setIndex(i);
                reminder.setWorkType(workType);
                reminder.setBeaconRegion(beaconRegion);
                reminder.setAlertActive(alertActive);
                reminder.setWasAutoActivated(wasAutoActivated);
                reminder.setLost(lost);
                reminder.setWithLostLocation(withLostLocation);
                reminder.setLostLocation(lostLocation);

                if(alertActive) activeReminderList.add(reminder);
                reminderList.add(reminder);
            }
        }
    }

    void restoreBeacons(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("beacons_shared_preferences", MODE_PRIVATE);
        int numberOfBeacons = sharedPreferences.getInt("beacons_number", 0);
        if(numberOfBeacons != 0){
            reminderBeacons.clear();
            for(int i = 0; i<numberOfBeacons; i++){
                String identifier = sharedPreferences.getString("beacon_identifier"+i,"0");
                String UUID = sharedPreferences.getString("beacon_UUID"+i,"0");
                int major = sharedPreferences.getInt("beacon_major_number"+i, 0);
                int minor = sharedPreferences.getInt("beacon_minor_number"+i, 0);

                if (identifier != null) {
                    BeaconRegion beaconRegion = new BeaconRegion(identifier, java.util.UUID.fromString(UUID), major, minor);
                    reminderBeacons.add(beaconRegion);
                }
            }
        }
    }

    void restoreUUID(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("UUID_shared_preferences", MODE_PRIVATE);
        int numberOfUUID = sharedPreferences.getInt("UUID_number", 0);
        if(numberOfUUID != 0){
            UUIDList.clear();
            for(int i = 0; i<numberOfUUID; i++){
                String UUID = sharedPreferences.getString("UUID"+i, "0");

                assert UUID != null;
                if(!UUID.equals("0")) UUIDList.add(UUID);
            }
        }
    }

    void resetReminders(Context context){
        saveReminders(context);
        restoreReminders(context);
    }

    void resetBeacons(Context context){
        saveBeacons(context);
        restoreBeacons(context);
    }

    void resetUUID(Context context){
        saveUUID(context);
        restoreUUID(context);
    }

    void checkPermissions(Context context) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(((Activity) context),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
        try {
            int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            if(locationMode < 3){
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    void dispatchTakePictureIntent(Context context, Reminder reminder) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(reminder.getType(), context);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.fixap.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile(String type, Context context) throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = type + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    boolean isWifiEnabled(Context context){
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    void changeWifiState(Context context, boolean enabled){
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }

    void makeAllItemNotActive(Context context){
        for(int i = 0; i<reminderList.size(); i++){
            if(reminderList.get(i).isAlertActive()) {
                reminderList.get(i).setAlertActive(false);
                activeReminderList.remove(reminderList.get(i));
            }
        }
        resetReminders(context);
    }

    @SuppressLint("ShortAlarm")
    void SendBroadcastToService(Context context, Intent intent, long triggerAtMillis, int intervalMillis, int flag){
        context.sendBroadcast(intent);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flag);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
    }
}
