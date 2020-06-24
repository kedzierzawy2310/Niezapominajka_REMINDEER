package com.example.fixap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.availableBeacons;
import static com.example.fixap.BeaconApplication.background;
import static com.example.fixap.BeaconApplication.reminderList;
import static com.example.fixap.MainActivity.activityFunctions;

public class ReminderAlert extends AppCompatActivity {

    private Reminder reminder;
    private final Handler handler = new Handler();

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_alert_layout);
        activityFunctions.restoreReminders(this);

        activityFunctions.hideStatusBar(this, null);

        ImageView alertReminderIcon = findViewById(R.id.reminderAlertIcon);
        TextView alertBeacon = findViewById(R.id.alertBeacon);
        TextView alertCurrentLocation = findViewById(R.id.alertCurrentLocation);
        TextView alertReminderType = findViewById(R.id.reminderAlertType);

        Intent intent = getIntent();
        reminder = intent.getParcelableExtra("alert");

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.cancel(100 + reminder.getId());


        if(reminder.getType().contains("OTHERS")){
            alertReminderType.setText(reminder.getOtherTypeName());
            alertReminderIcon.setImageResource(R.drawable.question_mark);
        }
        else{
            alertReminderType.setText(reminder.getType());
            switch(reminder.getType()){
                case "BACKPACK":
                    alertReminderIcon.setImageResource(R.drawable.backpack);
                    break;
                case "KEYS":
                    alertReminderIcon.setImageResource(R.drawable.key);
                    break;
                case "JACKET":
                    alertReminderIcon.setImageResource(R.drawable.jacket);
                    break;
                case "WALLET":
                    alertReminderIcon.setImageResource(R.drawable.wallet);
                    break;
                default:
                    break;
            }
        }

        if(!reminder.isWithLostLocation()) {
            alertCurrentLocation.setText("COULDN'T GET CURRENT LOCATION");
            alertCurrentLocation.setTextColor(Color.parseColor("#ff4444"));
        }
        else {
            alertCurrentLocation.setText(reminder.getLostLocation());
            alertCurrentLocation.setTextColor(Color.parseColor("#272759"));
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(!reminderList.get(reminder.getIndex()).isLost()) {
                    if(reminderList.get(reminder.getIndex()).getId() == reminder.getId()) {
                        finish();
                    }
                    else {
                        boolean exist = false;
                        for (int i = 0; i<reminderList.size(); i++){
                            if(reminderList.get(i).getId() == reminder.getId()){
                                exist = true;
                                if(!reminderList.get(i).isLost()) finish();
                            }
                        }
                        if(!exist) finish();
                    }
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);

        alertBeacon.setText(reminder.getBeaconRegion());
    }

    public void showPhoto(View view){
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.imagebutton_click_animation));
        if(reminder.getImage().equals("0")){
            Snackbar.make(view,"REMINDEER'S GOT NO PHOTO", Snackbar.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra("reminderImage", reminder);
            startActivity(intent);
        }
    }

    public void haveGotIt(View view){
        handler.removeCallbacksAndMessages(null);
        if(reminderList.get(reminder.getIndex()).getId() == reminder.getId()) {
            reminderList.get(reminder.getIndex()).setAlertActive(false);
            activeReminderList.remove(reminderList.get(reminder.getIndex()));
            reminderList.get(reminder.getIndex()).setLost(false);
        }
        else {
            for(int i = 0; i<reminderList.size(); i++){
                if(reminderList.get(i).getId() == reminder.getId()){
                    reminderList.get(i).setAlertActive(false);
                    activeReminderList.remove(reminderList.get(i));
                    reminderList.get(i).setLost(false);
                }
            }
        }
        activityFunctions.resetReminders(this);
        if(activeReminderList.isEmpty()) {
            stopService(new Intent(this, MyBeaconService.class));
            availableBeacons.clear();
        }
        finish();
    }

    public void haveNotGotIt(View view){
        handler.removeCallbacksAndMessages(null);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityFunctions.hideStatusBar(this, null);
        background = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityFunctions.hideStatusBar(this,null);
        background = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityFunctions.hideStatusBar(this, null);
        handler.removeCallbacksAndMessages(null);
    }

}
