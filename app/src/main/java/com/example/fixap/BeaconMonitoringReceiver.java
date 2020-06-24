package com.example.fixap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import static com.example.fixap.BeaconApplication.activeReminderList;

public class BeaconMonitoringReceiver extends BroadcastReceiver {
    public BeaconMonitoringReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Receiver works", Toast.LENGTH_SHORT).show();
        if(!activeReminderList.isEmpty()) {
            Intent intent1 = new Intent(context, MyBeaconService.class);
            ContextCompat.startForegroundService(context, intent1);
        }
    }
}