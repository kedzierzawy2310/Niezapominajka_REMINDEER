package com.example.fixap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.fixap.AddReminder.movedIn;
import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.background;
import static com.example.fixap.BeaconApplication.reminderList;
import static com.example.fixap.MainActivity.activityFunctions;
import static com.example.fixap.MainActivity.fromMainActivity;

public class ChooseReminder extends AppCompatActivity {

    private static final int ADD_REMINDER = 1;
    private int counter = 1;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_reminder_type_layout);
        activityFunctions.hideStatusBar(this, null);
        activityFunctions.restoreReminders(this);
        if(movedIn && activeReminderList.isEmpty()) {
            stopService(new Intent(this, MyBeaconService.class));
            movedIn = false;
        }
    }

    public void ChooseOne(View view){
        view.startAnimation(AnimationUtils.loadAnimation(this,R.anim.cardview_click_animation));
        try {
            TimeUnit.MILLISECONDS.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        restoreCounter();
        Reminder reminder = new Reminder("", counter);
        counter++;
        saveCounter();
        switch (view.getId()){
            case R.id.WalletChosen:
                reminder.setType(3);
                break;
            case R.id.KeysChosen:
                reminder.setType(2);
                break;
            case R.id.JacketChosen:
                reminder.setType(4);
                break;
            case R.id.BackpackChosen:
                reminder.setType(1);
                break;
            default:
                reminder.setType(5);
                break;
        }
        Intent intent = new Intent(this, AddReminder.class);
        intent.putExtra("reminder", reminder);
        intent.putExtra("activityFunctions", (Parcelable) activityFunctions);
        startActivityForResult(intent, ADD_REMINDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == ADD_REMINDER)
        {
            Reminder reminder = Objects.requireNonNull(data).getParcelableExtra("reminderAdd");
            if(fromMainActivity) {
                Intent intent = new Intent();
                intent.putExtra("reminderCompleted", reminder);
                setResult(RESULT_OK, intent);
                finish();
            }
            else {
                reminder.setIndex(reminderList.size());
                reminderList.add(reminder);
                Calendar calendar = Calendar.getInstance();
                calendar.set(reminder.getYearStart(), reminder.getMonthStart()-1, reminder.getDayStart(), reminder.getHourStart(), reminder.getMinuteStart());
                if(reminder.isWorkType() || calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    reminder.setAlertActive(true);
                    reminder.setWasAutoActivated(true);
                    activeReminderList.add(reminder);
                }
                activityFunctions.saveReminders(this);
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    private void saveCounter(){
        SharedPreferences sharedPreferences = getSharedPreferences("counter_shared_preferences", MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("counter", counter);
        editor.apply();
    }

    private void restoreCounter(){
        SharedPreferences sharedPreferences = getSharedPreferences("counter_shared_preferences", MODE_PRIVATE);
        int sharedCounter = sharedPreferences.getInt("counter", 0);
        if(sharedCounter != 0) counter = sharedCounter;
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
    protected void onStart() {
        super.onStart();
        activityFunctions.hideStatusBar(this, null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        activityFunctions.hideStatusBar(this, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityFunctions.hideStatusBar(this, null);
    }
}
