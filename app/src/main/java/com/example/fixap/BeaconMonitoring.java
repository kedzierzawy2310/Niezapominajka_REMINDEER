package com.example.fixap;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.fixap.BeaconApplication.UUIDList;
import static com.example.fixap.BeaconApplication.availableBeacons;
import static com.example.fixap.BeaconApplication.background;
import static com.example.fixap.BeaconApplication.mainDestroyed;
import static com.example.fixap.BeaconApplication.rangedBeaconRegions;
import static com.example.fixap.BeaconApplication.reminderBeacons;
import static com.example.fixap.BeaconApplication.reminderList;
import static com.example.fixap.BeaconApplication.adapter;
import static com.example.fixap.MainActivity.activityFunctions;

class BeaconMonitoring {
    @SuppressLint("StaticFieldLeak")
    private static BeaconManager beaconManager;
    @SuppressLint("StaticFieldLeak")
    private static BeaconManager secondBeaconManager;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static boolean beaconAssigned = false;

    //reminderBeacons.add(new BeaconRegion("pyraLab5", java.util.UUID.fromString(UUID), 17092, 29367));
    //reminderBeacons.add(new BeaconRegion("pyraLab9", java.util.UUID.fromString(UUID), 1376, 23309));

    static void Create(final Context context){

        mContext = context;

        final Calendar calendar = Calendar.getInstance();

        beaconManager = new BeaconManager(context);

        beaconManager.setForegroundScanPeriod(1000,0);
        beaconManager.setBackgroundScanPeriod(1000,0);

        beaconManager.setRegionExitExpiration(1000);

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                Toast.makeText(context, reminderList.size() + " - Items",
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(context, beaconRegion.getIdentifier()
                                + " - Entered",
                        Toast.LENGTH_SHORT).show();
                for (int i = 0; i<reminderList.size(); i++){
                    Reminder reminder = reminderList.get(i);
                    if(reminder.getBeaconRegion()
                            .equals(beaconRegion.getIdentifier())) {
                        beaconAssigned = true;
                        if(reminder.isLost()){
                            //reminder.setLost(false);
                            getLocation(context, reminder, true);
                        }
                    }
                }
                if(!beaconAssigned) availableBeacons.add(beaconRegion);
                adapter.notifyDataSetChanged();
                beaconAssigned = false;
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion) {
                Toast.makeText(context, beaconRegion.getIdentifier() + " - Exited",
                        Toast.LENGTH_SHORT).show();
                for (int i = 0; i<reminderList.size(); i++) {
                    Reminder reminder = reminderList.get(i);
                    if(reminder.getBeaconRegion().equals(beaconRegion.getIdentifier()))
                    {
                        beaconAssigned = true;
                        if(reminder.isAlertActive())
                        {
                            if (reminder.isWorkType())
                            {
                                //reminder.setLost(true);
                                getLocation(context, reminder, false);
                            } else {
                                calendar.set(reminder.getYearStart(),
                                        reminder.getMonthStart() - 1,
                                        reminder.getDayStart(),
                                        reminder.getHourStart(),
                                        reminder.getMinuteStart());
                                if (System.currentTimeMillis() > calendar.getTimeInMillis())
                                {
                                    calendar.set(reminder.getYearEnd(),
                                            reminder.getMonthEnd() - 1,
                                            reminder.getDayEnd(),
                                            reminder.getHourEnd(),
                                            reminder.getMinuteEnd());
                                    if (System.currentTimeMillis() < calendar.getTimeInMillis())
                                    {
                                        //reminder.setLost(true);
                                        getLocation(context, reminder, false);
                                    }
                                }
                            }
                        }
                    }
                }
                if(!beaconAssigned) availableBeacons.remove(beaconRegion);
                if(adapter.getBeacon().equals(beaconRegion.getIdentifier()))
                    adapter.setBeacon("0");
                adapter.notifyDataSetChanged();
                beaconAssigned = false;
            }
        });
    }

    static void Start(){
        beaconManager
                .connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    for(int i = 0; i<reminderBeacons.size(); i++) {
                        beaconManager
                                .startMonitoring(reminderBeacons
                                        .get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void Stop(){
        beaconManager.disconnect();
    }

    static void SetupRanging(Context context){
        secondBeaconManager = new BeaconManager(context);
        secondBeaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {

            }
        });
        if(!UUIDList.isEmpty()){
            for(int i = 0; i<UUIDList.size(); i++){
                BeaconRegion beaconRegion = new BeaconRegion("ranged "
                        + i, java.util.UUID
                        .fromString(UUIDList.get(i)), null, null);
                rangedBeaconRegions.add(beaconRegion);
            }
        }
    }

    static void TearDownRanging(){
        rangedBeaconRegions.clear();
    }

    static void StartRanging(){
        secondBeaconManager.connect(new BeaconManager
                .ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    for (int i = 0; i < rangedBeaconRegions.size();
                         i++) {
                        secondBeaconManager
                                .startRanging(rangedBeaconRegions
                                        .get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void StopRanging(){
        try {
            for (int i = 0; i < rangedBeaconRegions.size(); i++) {
                secondBeaconManager.stopRanging(rangedBeaconRegions.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        secondBeaconManager.disconnect();
    }


    private static void startAlert(Reminder reminder){
        Intent intent = new Intent(mContext, ReminderAlert.class);
        intent.putExtra("alert", reminder);
        mContext.startActivity(intent);
    }

    private static void startNotificationAlert(Reminder reminder, boolean isLost){
        String title;
        String text;
        int color;
        int icon;
        Intent newIntent;

        if(isLost){
            title = "WAIT! YOU'VE LEFT: " + reminder.getType();
            text = "Tap to show more details";
            color = Color.RED;
            newIntent = new Intent(mContext, ReminderAlert.class);
            newIntent.putExtra("alert", reminder);
            icon = android.R.drawable.ic_dialog_alert;
        }
        else {
            title = "UFF... YOU'VE ALMOST LEFT YOUR: "
                    + reminder.getType();
            text = "Tap to open ReminDEER";
            color = Color.YELLOW;
            newIntent = new Intent(mContext, MainActivity.class);
            icon = android.R.drawable.ic_dialog_info;
        }
        Notification.Builder builder
                = new Notification.Builder(mContext);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        builder.setAutoCancel(true);
        builder.setVibrate(new long[] { 1000, 1000 });
        builder.setLights(color, 3000, 3000);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(mContext, 0, newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(mContext);
        int ALERT_NOTIFICATION_ID = 100;
        managerCompat.notify(ALERT_NOTIFICATION_ID + reminder.getId(), notification);
    }

    private static void getLocation(Context context, final Reminder newReminder, final boolean entered){
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            if (connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .isConnected()
                    || connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
                locationManager
                        .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Geocoder geocoder = new Geocoder(mContext,
                                        Locale.getDefault());
                                try {
                                    List<Address> addresses
                                            = geocoder.getFromLocation(latitude,
                                            longitude, 1);
                                    String address = addresses.get(0).getAddressLine(0);
                                    if(entered){
                                        newReminder.setLost(false);
                                        newReminder.setLostLocation("0");
                                        newReminder.setLocation(address);
                                        newReminder.setWithLostLocation(false);
                                        activityFunctions.resetReminders(mContext);
                                        if(background) startNotificationAlert(newReminder, false);
                                        else if(!mainDestroyed)
                                            ReminderListFragment.notifyDataSetChanged();
                                    } else {
                                        newReminder.setLost(true);
                                        newReminder.setLostLocation(address);
                                        newReminder.setWithLostLocation(true);
                                        activityFunctions.resetReminders(mContext);
                                        if(background) startNotificationAlert(newReminder, true);
                                        else if(!mainDestroyed) ReminderListFragment.notifyDataSetChanged();
                                        startAlert(newReminder);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                locationManager.removeUpdates(this);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {

                            }

                            @Override
                            public void onProviderEnabled(String provider) {

                            }

                            @Override
                            public void onProviderDisabled(String provider) {

                            }
                        });
            } else {
                if(entered) newReminder.setLostLocation("0");
                newReminder.setWithLostLocation(false);
                if(entered){
                    newReminder.setLost(false);
                    if(background) startNotificationAlert(newReminder, false);
                    else if(!mainDestroyed) ReminderListFragment.notifyDataSetChanged();
                } else {
                    newReminder.setLost(true);
                    if(background) startNotificationAlert(newReminder, true);
                    else if(!mainDestroyed) ReminderListFragment.notifyDataSetChanged();
                    startAlert(newReminder);
                }
                activityFunctions.resetReminders(context);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
