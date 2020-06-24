package com.example.fixap;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.adapter;
import static com.example.fixap.BeaconApplication.availableBeacons;
import static com.example.fixap.BeaconApplication.mainDestroyed;
import static com.example.fixap.BeaconApplication.reminderList;
import static com.example.fixap.MainActivity.activityFunctions;

public final class MyBeaconService extends Service {

    BluetoothAdapter bluetoothAdapter;
    BroadcastReceiver broadcastReceiver;
    Notification notification;
    Intent intent1;
    PendingIntent pendingIntent;
    boolean done;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        activityFunctions.checkPermissions(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        intent1 = new Intent(getApplicationContext(),
                MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);
        done = false;

        if(bluetoothAdapter.isEnabled()) {
            done = true;
            notification = new NotificationCompat.Builder(this,
                    "BeaconsMonitoringChannel")
                    .setContentTitle("Your ReminDEERs are monitored")
                    .setContentText("Active ReminDEERs: "
                            + activeReminderList.size())
                    .setSmallIcon(R.drawable.ibeacon_transmiton)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);

            BeaconMonitoring.Create(this);
            BeaconMonitoring.Start();
            BeaconMonitoring.SetupRanging(this);
            BeaconMonitoring.StartRanging();
        } else {
            stopSelf();
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Intent intentService = new Intent(context,
                        MyBeaconService.class);
                final String currentAction = intent.getAction();
                assert currentAction != null;
                if(currentAction.equals(BluetoothAdapter
                        .ACTION_STATE_CHANGED)){
                    final int currentState = intent
                            .getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                    BluetoothAdapter.ERROR);
                    if(currentState
                            == BluetoothAdapter.STATE_TURNING_OFF){
                        if(!activeReminderList.isEmpty()){
                            for(int i = 0; i<reminderList.size(); i++){
                                if(!reminderList.get(i).isLost()) {
                                    reminderList.get(i)
                                            .setAlertActive(false);
                                }
                            }
                            activityFunctions.resetReminders(context);
                            if(!mainDestroyed) ReminderListFragment
                                    .notifyDataSetChanged();
                        }
                        stopService(intentService);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter
                .ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags,
                              int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        availableBeacons.clear();
        adapter.notifyDataSetChanged();
        if(done) {
            BeaconMonitoring.Stop();
            BeaconMonitoring.StopRanging();
            BeaconMonitoring.TearDownRanging();
        }
        if(!bluetoothAdapter.isEnabled())
            makeNoBluetoothNotification();
        unregisterReceiver(broadcastReceiver);
    }

    private void makeNoBluetoothNotification(){
        notification = new NotificationCompat.Builder(this,
                "BeaconsMonitoringChannel")
                .setContentTitle("Bluetooth is not enabled!")
                .setContentText("Tap to turn on Bluetooth and start "
                        + "monitoring")
                .setSmallIcon(android.R.drawable
                        .stat_sys_data_bluetooth)
                .setVibrate(new long[] { 1000, 1000 })
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat managerCompat
                = NotificationManagerCompat.from(this);
        managerCompat.notify(1, notification);
    }
}
