package com.example.fixap;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BeaconReceiver extends BroadcastReceiver {
    private Intent beaconIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String currentAction = intent.getAction();
        if(currentAction.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            final int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (currentState){
                case BluetoothAdapter.STATE_ON:
                    if(beaconIntent == null){
                        beaconIntent = new Intent();
                        context.startService(beaconIntent);
                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    if(beaconIntent != null){
                        context.stopService(beaconIntent);
                        beaconIntent = null;
                    }
                    break;
            }
        }
    }
}
