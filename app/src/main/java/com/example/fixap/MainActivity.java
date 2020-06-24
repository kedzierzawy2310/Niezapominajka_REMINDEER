package com.example.fixap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.example.fixap.AddReminder.REQUEST_TAKE_PHOTO;
import static com.example.fixap.AddReminder.currentPhotoPath;
import static com.example.fixap.AddReminder.movedIn;
import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.availableBeacons;
import static com.example.fixap.BeaconApplication.background;
import static com.example.fixap.BeaconApplication.mainDestroyed;
import static com.example.fixap.BeaconApplication.reminderList;
import static com.example.fixap.RemindersIntentService.changed;

public class MainActivity extends AppCompatActivity implements ReminderListFragment.OnReminderListFragmentInteractionListener {

    private static final int CHOOSE_REMINDER = 0;
    private static final int REQUEST_ENABLE_BLUETOOTH = 15;
    final private static List<Reminder> checkedList = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    final public static ActivityFunctions activityFunctions = new ActivityFunctions();
    public static boolean fromMainActivity;
    private boolean isDeleting;
    private boolean turnOnBT;
    private ImageView deleteImageView;
    private Button addButton;
    private Button deleteButton;
    private LinearLayout linearLayout;
    private View fragment;
    private Reminder reminderWithoutImage;
    private Intent intentService;
    final private Handler handler = new Handler();
    private BroadcastReceiver broadcastReceiver;
    private BluetoothAdapter bluetoothAdapter;

    @SuppressLint("ShortAlarm")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainDestroyed = false;
        turnOnBT = true;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(!connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
                && !connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
            final AlertDialog.Builder builder2 = new AlertDialog.Builder(this).setTitle("Do you want to turn on DATA TRANSFER?").setMessage("If you select NO, you won't can use the additional support of getting lost-item location.").setCancelable(false).setPositiveButton("TURN ON", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog.Builder builder1 = new AlertDialog.Builder(this).setTitle("Do you want to turn on WIFI?").setMessage("It'll help you find items if they get lost. If you prefer use mobile data, select NO.").setCancelable(false).setPositiveButton("TURN ON", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog alertDialog2 = builder2.create();
                    activityFunctions.hideStatusBar(null, alertDialog2);
                    alertDialog2.show();
                }
            });

            final AlertDialog alertDialog1 = builder1.create();
            activityFunctions.hideStatusBar(null, alertDialog1);
            alertDialog1.show();
        }

        intentService = new Intent(this, MyBeaconService.class);

        deleteImageView = findViewById(R.id.DeleteImageButton);
        addButton = findViewById(R.id.Add);
        deleteButton = findViewById(R.id.Delete);
        linearLayout = findViewById(R.id.NoRemindersLayout);
        fragment = findViewById(R.id.reminder_list_fragment);
        deleteImageView.setVisibility(View.GONE);

        activityFunctions.restoreBeacons(this);
        checkReminderList();

        if(movedIn && activeReminderList.isEmpty()) {
            stopService(intentService);
            movedIn = false;
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String currentAction = intent.getAction();
                assert currentAction != null;
                if(currentAction.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                    final int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (currentState){
                        case BluetoothAdapter.STATE_ON:
                            ReminderListFragment.notifyDataSetChanged();
                            if(!activeReminderList.isEmpty()) startService(intentService);
                        break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            ReminderListFragment.notifyDataSetChanged();
                            break;
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void AddButton(View view){
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cardview_click_animation));
        fromMainActivity = true;
        Intent intent = new Intent(this, ChooseReminder.class);
        intent.putExtra("activityFunctions", (Parcelable) activityFunctions);
        startActivityForResult(intent, CHOOSE_REMINDER);
    }

    public void DeleteButton(View view){
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cardview_click_animation));
        setDeleting(true);
        addButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        deleteImageView.setVisibility(View.VISIBLE);
        ReminderListFragment.notifyDataSetChanged();
    }

    public void DeleteImageButton(View view){
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.imagebutton_click_animation));
        if(checkedList.isEmpty()){
            Snackbar.make(view, "NO ITEM SELECTED!", Snackbar.LENGTH_SHORT).show();
        }
        else {
            String string;
            if(checkedList.size()==1){
                string = "Are you sure you want to delete this " + checkedList.size() + " element?";
            }
            else {
                string = "Are you sure you want to delete these " + checkedList.size() + " elements?";
            }
            AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(string).setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteChecked();
                    checkedList.clear();
                    ReminderListFragment.notifyDataSetChanged();
                    deleteImageView.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.VISIBLE);
                    addButton.setVisibility(View.VISIBLE);
                    setDeleting(false);
                    checkReminderList();
                }
            }).setNegativeButton("CANCEL", null).setIcon(R.drawable.alert).create();
            activityFunctions.hideStatusBar(null, alertDialog);
            alertDialog.show();
            Objects.requireNonNull(alertDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    @Override
    public void onBackPressed(){
        if(isDeleting) {
            ReminderListFragment.notifyDataSetChanged();
            deleteImageView.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
            checkedList.clear();
            setDeleting(false);
        }
        else{
            finish();
        }
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Reminder reminder;
        if(resultCode == RESULT_OK){
            switch (requestCode) {
                case CHOOSE_REMINDER:
                    fromMainActivity = false;
                    reminder = Objects.requireNonNull(data).getParcelableExtra("reminderCompleted");
                    reminder.setIndex(reminderList.size());
                    reminderList.add(reminder);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(reminder.getYearStart(), reminder.getMonthStart()-1, reminder.getDayStart(), reminder.getHourStart(), reminder.getMinuteStart());
                    if(reminder.isWorkType() || calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        reminder.setAlertActive(true);
                        reminder.setWasAutoActivated(true);
                        activeReminderList.add(reminder);
                        stopService(intentService);
                        availableBeacons.clear();
                        startService(intentService);
                    }
                    activityFunctions.resetReminders(this);
                    ReminderListFragment.notifyDataSetChanged();
                    break;
                case REQUEST_TAKE_PHOTO:
                    reminderWithoutImage.setImage(currentPhotoPath);
                    activityFunctions.resetReminders(this);
                    ReminderListFragment.notifyDataSetChanged();
                    break;
                case REQUEST_ENABLE_BLUETOOTH:
                    bluetoothAdapter.enable();
                    turnOnBT = true;
                    Snackbar.make((findViewById(android.R.id.content)), "BLUETOOTH HAS BEEN TURNED ON", Snackbar.LENGTH_SHORT).show();
                    break;
                    default:
                        break;
            }
        } else {
          if(requestCode == REQUEST_ENABLE_BLUETOOTH){
              turnOnBT = false;
              Snackbar.make((findViewById(android.R.id.content)), "APP CANNOT WORK WITHOUT BLUETOOTH!", Snackbar.LENGTH_LONG).show();
          }
        }
    }

    private void checkReminderList(){
        if(reminderList.isEmpty()){
            linearLayout.setVisibility(View.VISIBLE);
            fragment.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
        else{
            linearLayout.setVisibility(View.GONE);
            fragment.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
        activityFunctions.hideStatusBar(this, null);
    }

    @Override
    public void putChecked(Reminder reminder) {
        checkedList.add(reminder);
    }

    @Override
    public void pullChecked(Reminder reminder) {
        checkedList.remove(reminder);
    }

    @Override
    public void getItemToTakeAPhoto(Reminder reminder) {
        reminderWithoutImage = reminder;
    }

    @Override
    public void deleteChecked() {
        reminderList.removeAll(checkedList);
        activityFunctions.resetReminders(this);
        if(reminderList.isEmpty() || activeReminderList.isEmpty()) {
            activeReminderList.clear();
            stopService(intentService);
        }
    }

    @Override
    public void setDeleting(boolean b) {
        isDeleting = b;
    }

    @Override
    public boolean checkIsDeleting() {
        return isDeleting;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityFunctions.saveBeacons(this);
        mainDestroyed = true;
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityFunctions.hideStatusBar(this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        background = false;
        activityFunctions.restoreReminders(this);
        activityFunctions.hideStatusBar(this, null);
        if(!isDeleting) {
            checkReminderList();
        }

        boolean enabled = bluetoothAdapter.isEnabled();
        if(!enabled && turnOnBT){
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
        }
        ReminderListFragment.notifyDataSetChanged();
        if(!activeReminderList.isEmpty()) {
            if(enabled) {
                stopService(intentService);
                availableBeacons.clear();
            }
            startService(intentService);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(changed){
                    ReminderListFragment.notifyDataSetChanged();
                    changed = false;
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable,1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        background = true;
        activityFunctions.saveReminders(this);
        activityFunctions.hideStatusBar(this,null);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityFunctions.hideStatusBar(this, null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        activityFunctions.hideStatusBar(this, null);
    }
}