package com.example.fixap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.example.fixap.BeaconApplication.UUIDList;
import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.adapter;
import static com.example.fixap.BeaconApplication.availableBeacons;
import static com.example.fixap.BeaconApplication.background;
import static com.example.fixap.BeaconApplication.reminderBeacons;
import static com.example.fixap.BeaconApplication.reminderList;
import static com.example.fixap.BeaconApplication.UUID;

public class AddReminder extends AppCompatActivity implements Switch.OnCheckedChangeListener, LocationListener {

    public static final int REQUEST_TAKE_PHOTO = 3;
    public static final int REQUEST_LOCATION = 2;
    public static String currentPhotoPath;
    public static boolean movedIn;
    private LocationManager locationManager;
    private EditText editText;
    private TextView textView;
    private TextView locationTextView;
    private TextView beaconTextView;
    private TextView textViewFrom;
    private TextView textViewTo;
    private Switch aSwitch;
    private TextView textType;
    private CardView dateFromCard;
    private CardView dateToCard;
    private Reminder reminder;
    private ImageView addImageView;
    private Calendar calendar;
    private ProgressBar progressBar;
    private ActivityFunctions activityFunctions;
    private BroadcastReceiver broadcastReceiver;
    private Intent intentService;
    private int yearE;
    private int monthE;
    private int dayE;
    private int hourE;
    private int minuteE;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_reminder_layout);
        Objects.requireNonNull(getSupportActionBar()).hide();

        editText = findViewById(R.id.reminderEditTextViewLarge);
        textView = findViewById(R.id.reminderTextViewLarge);
        ImageView imageView = findViewById(R.id.reminderImageLarge);
        aSwitch = findViewById(R.id.typeSwitch);
        textType = findViewById(R.id.typeTextView);
        dateFromCard = findViewById(R.id.DateFromCard);
        dateToCard = findViewById(R.id.DateToCard);
        addImageView = findViewById(R.id.AddReminderImageButton);
        locationTextView = findViewById(R.id.Localization);
        progressBar = findViewById(R.id.LocationProgressBar);
        beaconTextView = findViewById(R.id.Beacon);

        progressBar.setVisibility(View.GONE);

        Intent data = getIntent();
        reminder = data.getParcelableExtra("reminder");
        activityFunctions = data.getParcelableExtra("activityFunctions");

        activityFunctions.hideStatusBar(this, null);

        aSwitch.setOnCheckedChangeListener(this);
        onCheckedChanged(aSwitch, false);

        if (!reminder.getType().contains("OTHERS")) {
            textView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            textView.setText(reminder.getType());
        } else {
            editText.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }
        switch (reminder.getType()) {
            case "BACKPACK":
                imageView.setImageResource(R.drawable.backpack);
                break;
            case "KEYS":
                imageView.setImageResource(R.drawable.key);
                break;
            case "JACKET":
                imageView.setImageResource(R.drawable.jacket);
                break;
            case "WALLET":
                imageView.setImageResource(R.drawable.wallet);
                break;
            default:
                imageView.setImageResource(R.drawable.question_mark);
                break;
        }
        movedIn = false;
        intentService = new Intent(this, MyBeaconService.class);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String currentAction = intent.getAction();
                assert currentAction != null;
                if(currentAction.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                    final int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (currentState){
                        case BluetoothAdapter.STATE_ON:
                            if(movedIn) startService(intentService);
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @SuppressLint("SetTextI18n")
    public void SetDate(final View view) {
        calendar = Calendar.getInstance();
        yearE = calendar.get(Calendar.YEAR);
        monthE = calendar.get(Calendar.MONTH);
        dayE = calendar.get(Calendar.DAY_OF_MONTH);
        hourE = calendar.get(Calendar.HOUR_OF_DAY);
        minuteE = calendar.get(Calendar.MINUTE);
        final TextView textView;
        textViewFrom = findViewById(R.id.FromDate);
        textViewTo = findViewById(R.id.ToDate);

        if (dateFromCard.isPressed()) {
            textView = textViewFrom;
            reminder.setYearStart(0);
            reminder.setMonthStart(0);
            reminder.setDayStart(0);
            reminder.setHourStart(-1);
            reminder.setMinuteStart(-1);
        } else {
            textView = textViewTo;
            reminder.setYearEnd(0);
            reminder.setMonthEnd(0);
            reminder.setDayEnd(0);
            reminder.setHourEnd(-1);
            reminder.setMinuteEnd(-1);
        }

        final TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                DecimalFormat format = new DecimalFormat("00");
                String hour = format.format(hourOfDay);
                String minutes = format.format(minute);
                String fullDate = hour + ":" + minutes;
                textView.setText(textView.getText() + "  " + fullDate);
                switch (view.getId()) {
                    case R.id.DateFromCard:
                        reminder.setHourStart(hourOfDay);
                        reminder.setMinuteStart(minute);
                        reminder.setStartDate(textView.getText().toString());
                        if ((reminder.getYearStart() == yearE && reminder.getMonthStart() == monthE + 1 && reminder.getDayStart() == dayE && (reminder.getHourStart() < hourE
                                || (reminder.getHourStart() == hourE && reminder.getMinuteStart() < minuteE)))) {
                            reminder.setYearStart(0);
                            reminder.setMonthStart(0);
                            reminder.setDayStart(0);
                            reminder.setHourStart(-1);
                            reminder.setMinuteStart(-1);
                            textView.setText(null);
                            Snackbar.make(view, "WRONG TIME HAS BEEN SELECTED!", Snackbar.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.DateToCard:
                        reminder.setHourEnd(hourOfDay);
                        reminder.setMinuteEnd(minute);
                        reminder.setEndDate(textView.getText().toString());
                        if ((reminder.getYearEnd() == yearE && reminder.getMonthEnd() == monthE + 1 && reminder.getDayEnd() == dayE && (reminder.getHourEnd() < hourE
                                || (reminder.getHourEnd() == hourE && reminder.getMinuteEnd() < minuteE + 1)))) {
                            reminder.setYearEnd(0);
                            reminder.setMonthEnd(0);
                            reminder.setDayEnd(0);
                            reminder.setHourEnd(-1);
                            reminder.setMinuteEnd(-1);
                            textView.setText(null);
                            Snackbar.make(view, "WRONG TIME HAS BEEN SELECTED!", Snackbar.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }, hourE, minuteE, true);

        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
            public void onDismiss(DialogInterface dialogInterface) {
                switch (view.getId()){
                    case R.id.DateFromCard:
                        if(reminder.getHourStart() == -1 && reminder.getMinuteStart() == -1){
                            textView.setText("");
                            reminder.setDayStart(0);
                            reminder.setMonthStart(0);
                            reminder.setYearStart(0);
                        }
                        break;
                    case R.id.DateToCard:
                        if(reminder.getHourEnd() == -1 && reminder.getMinuteEnd() == -1){
                            textView.setText("");
                            reminder.setDayEnd(0);
                            reminder.setMonthEnd(0);
                            reminder.setYearEnd(0);
                        }
                        break;
                }
            }
        });

        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                DecimalFormat format = new DecimalFormat("00");
                String months = format.format(month + 1);
                String days = format.format(dayOfMonth);
                textView.setText(days + "/" + months + "/" + year);
                switch (view.getId()) {
                    case R.id.DateFromCard:
                        reminder.setYearStart(year);
                        reminder.setMonthStart(month + 1);
                        reminder.setDayStart(dayOfMonth);
                        break;
                    case R.id.DateToCard:
                        reminder.setYearEnd(year);
                        reminder.setMonthEnd(month + 1);
                        reminder.setDayEnd(dayOfMonth);
                        break;
                }
                timePickerDialog.show();
            }
        }, 0, 0, 0);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (aSwitch.isChecked()) {
            textType.setText("REMIND ALL TIME");
            dateFromCard.setVisibility(View.INVISIBLE);
            dateToCard.setVisibility(View.INVISIBLE);
            reminder.setWorkType(true);
        } else {
            textType.setText("REMIND FOR A WHILE");
            dateFromCard.setVisibility(View.VISIBLE);
            dateToCard.setVisibility(View.VISIBLE);
            reminder.setWorkType(false);
        }
    }

    public static void CheckPermissions(Context context) {
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

    public void BuildAlert(){
        final AlertDialog.Builder builder2 = new AlertDialog.Builder(this).setTitle("Do you want to turn on DATA TRANSFER?").setMessage("If you select NO, I won't can get current location.").setCancelable(false).setPositiveButton("TURN ON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                locationTextView.setText("CAN'T GET CURRENT LOCATION");
                locationTextView.setTextColor(Color.parseColor("#ff4444"));
                dialog.dismiss();
            }
        });
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(this).setTitle("Do you want to turn on WIFI?").setMessage("If you can't / don't want to use this type of getting location, select NO.").setCancelable(false).setPositiveButton("TURN ON", new DialogInterface.OnClickListener() {
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

    public void GetAddingLocation(View view){
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cardview_click_animation));
        CheckPermissions(this);
        try{
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
                    || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
                progressBar.setVisibility(View.VISIBLE);
            }
            else BuildAlert();
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void SendIntent(){
        if(!reminder.isWorkType()){
            Calendar calendar = Calendar.getInstance();
            calendar.set(reminder.getYearStart(), reminder.getMonthStart()-1, reminder.getDayStart(), reminder.getHourStart(), reminder.getMinuteStart());
            if(System.currentTimeMillis() >= calendar.getTimeInMillis()) calendar.set(reminder.getYearEnd(), reminder.getMonthEnd()-1, reminder.getDayEnd(), reminder.getHourEnd(), reminder.getMinuteEnd());
            Intent intent = new Intent(this, RemindersBroadcastReceiver.class);
            sendBroadcast(intent);
            PendingIntent pendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            }
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        }
        if(reminder.getType().contains("OTHERS")) reminder.setOtherTypeName(editText.getText().toString());
        Intent intent = new Intent();
        intent.putExtra("reminderAdd", reminder);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void CreateReminder(final View view) {
        boolean completedDataFrom;
        boolean completedDataTo;
        textView = findViewById(R.id.FromDate);
        completedDataFrom = !textView.getText().toString().isEmpty();
        textView = findViewById(R.id.ToDate);
        completedDataTo = !textView.getText().toString().isEmpty();

        view.startAnimation(AnimationUtils.loadAnimation(this,R.anim.imagebutton_click_animation));

        if((reminder.getYearEnd() == reminder.getYearStart() && reminder.getMonthEnd() == reminder.getMonthStart() && reminder.getDayEnd() < reminder.getDayStart())){
            reminder.setYearStart(0);
            reminder.setMonthStart(0);
            reminder.setDayStart(0);
            reminder.setHourStart(0);
            reminder.setMinuteStart(0);
            reminder.setYearEnd(0);
            reminder.setMonthEnd(0);
            reminder.setDayEnd(0);
            reminder.setHourEnd(0);
            reminder.setMinuteEnd(0);
            textViewFrom.setText(null);
            textViewTo.setText(null);
            Snackbar.make(view, "WRONG TIME INTERVAL WAS SELECTED!", Snackbar.LENGTH_SHORT).show();
        }

        else if((reminder.isWorkType() || (completedDataFrom && completedDataTo))
                && (!reminder.getType().contains("OTHERS") || !editText.getText().toString().isEmpty())
                && !beaconTextView.getText().toString().isEmpty()){
            calendar = Calendar.getInstance();
            yearE = calendar.get(Calendar.YEAR);
            monthE = calendar.get(Calendar.MONTH);
            dayE = calendar.get(Calendar.DAY_OF_MONTH);
            hourE = calendar.get(Calendar.HOUR_OF_DAY);
            minuteE = calendar.get(Calendar.MINUTE);

            DecimalFormat format = new DecimalFormat("00");
            String month = format.format(monthE+1);
            String day = format.format(dayE);
            String hour = format.format(hourE);
            String minute = format.format(minuteE);

            reminder.setMadeDate(day + "/" + month + "/" + yearE + "  " + hour + ":" + minute );
            if(!locationTextView.getText().toString().equals("CAN'T GET CURRENT LOCATION")
                    && !locationTextView.getText().toString().equals("")) reminder.setLocation(locationTextView.getText().toString());

            if(reminder.getLocation() == null){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("You've forgotten about location").setMessage("Getting location will help you in case of lost item. Are you sure you want to skip it?").setCancelable(false).setPositiveButton("SKIP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SendIntent();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                activityFunctions.hideStatusBar(null, alertDialog);
                alertDialog.show();
                Objects.requireNonNull(alertDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
            else SendIntent();
        }
        else Snackbar.make(view, "COMPLETE ALL DATA!", Snackbar.LENGTH_SHORT).show();
    }

    public void ChooseReminderBeacon(final View view){
        view.startAnimation(AnimationUtils.loadAnimation(this,R.anim.cardview_click_animation));
        movedIn = true;
        if(!UUIDList.isEmpty()) UUID = UUIDList.get(0);

        if(activeReminderList.isEmpty()) startService(intentService);

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View addDialogView = inflater.inflate(R.layout.add_beacon_dialog,null);

        final EditText beaconRegionEditText = addDialogView.findViewById(R.id.beaconRegionEditText);
        final EditText UUIDEditText = addDialogView.findViewById(R.id.UUIDEditText);
        final EditText MajorEditText = addDialogView.findViewById(R.id.MajorEditText);
        final EditText MinorEditText = addDialogView.findViewById(R.id.MinorEditText);
        CheckBox checkBox = addDialogView.findViewById(R.id.checkboxUUID);
        final CheckBox setUUIDCheckbox = addDialogView.findViewById(R.id.checkboxSetDefaultUUID);

        final float textSize = 13;
        final boolean[] setDefault = {false};
        String removeButton;

        if(!reminderBeacons.isEmpty()) removeButton = "REMOVE ALL";
        else removeButton = "";

        beaconRegionEditText.setTextSize(textSize);
        UUIDEditText.setTextSize(textSize);
        MajorEditText.setTextSize(textSize);
        MinorEditText.setTextSize(textSize);

        setUUIDCheckbox.setOnCheckedChangeListener(null);
        if(UUID == null) {
            setUUIDCheckbox.setChecked(true);
            setDefault[0] = true;
        }
        else setUUIDCheckbox.setChecked(false);
        setUUIDCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDefault[0] = isChecked;
            }
        });

        checkBox.setOnCheckedChangeListener(null);
        if(UUID == null){
            checkBox.setChecked(false);
            checkBox.setVisibility(View.INVISIBLE);
            UUIDEditText.setEnabled(true);
            UUIDEditText.setText("");
            setUUIDCheckbox.setVisibility(View.VISIBLE);
        }
        else {
            checkBox.setChecked(true);
            checkBox.setVisibility(View.VISIBLE);
            UUIDEditText.setEnabled(false);
            UUIDEditText.setText(UUID);
            setUUIDCheckbox.setVisibility(View.INVISIBLE);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    UUIDEditText.setEnabled(false);
                    UUIDEditText.setText(UUID);
                    setUUIDCheckbox.setVisibility(View.GONE);
                }
                else {
                    UUIDEditText.setEnabled(true);
                    UUIDEditText.setText("");
                    setUUIDCheckbox.setVisibility(View.VISIBLE);
                }
            }
        });

        final AlertDialog.Builder removeBuilder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Are you sure you want to remove all Beacons?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reminderBeacons.clear();
                        availableBeacons.clear();
                        activityFunctions.resetBeacons(view.getContext());
                        reminderList.clear();
                        activeReminderList.clear();
                        activityFunctions.resetReminders(view.getContext());
                        stopService(intentService);
                        availableBeacons.clear();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ChooseReminderBeacon(view);
                    }
                });

        final AlertDialog.Builder addBuilder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Add BEACON")
                .setView(addDialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!beaconRegionEditText.getText().toString().isEmpty()
                                && !MajorEditText.getText().toString().isEmpty()
                                && !MinorEditText.getText().toString().isEmpty()
                                && ((!UUIDEditText.getText().toString().isEmpty()
                                && UUIDEditText.isEnabled())
                                || !UUIDEditText.isEnabled())){
                            int major;
                            int minor;
                            try{
                                major = Integer.parseInt(MajorEditText.getText().toString());
                            } catch (Exception e) {
                                major = 0;
                            }
                            try{
                                minor = Integer.parseInt(MinorEditText.getText().toString());
                            } catch (NumberFormatException e) {
                                minor = 0;
                            }
                            if ((major == 0 || major > 65535) && (minor == 0 || minor > 65535) && (UUIDEditText.getText().toString().length() != 32 && UUIDEditText.isEnabled())) Snackbar.make(view,"WRONG BEACON PARAMETERS! (UUID, MAJOR & MINOR)", Snackbar.LENGTH_SHORT).show();
                            else if((major == 0 || major > 65535) && (minor == 0 || minor > 65535)) Snackbar.make(view,"WRONG BEACON PARAMETERS! (MAJOR & MINOR)", Snackbar.LENGTH_SHORT).show();
                            else if((major == 0 || major > 65535) && (UUIDEditText.getText().toString().length() != 32 && UUIDEditText.isEnabled())) Snackbar.make(view,"WRONG BEACON PARAMETERS! (MAJOR & UUID)", Snackbar.LENGTH_SHORT).show();
                            else if((minor == 0 || minor > 65535) && (UUIDEditText.getText().toString().length() != 32 && UUIDEditText.isEnabled())) Snackbar.make(view,"WRONG BEACON PARAMETERS! (MINOR & UUID)", Snackbar.LENGTH_SHORT).show();
                            else if(major == 0 || major > 65535) Snackbar.make(view,"WRONG BEACON PARAMETER! (MAJOR)", Snackbar.LENGTH_SHORT).show();
                            else if(minor == 0 || minor > 65535) Snackbar.make(view,"WRONG BEACON PARAMETER! (MINOR)", Snackbar.LENGTH_SHORT).show();
                            else if(UUIDEditText.getText().toString().length() != 32 && UUIDEditText.isEnabled()) Snackbar.make(view, "WRONG BEACON PARAMETER! (UUID)", Snackbar.LENGTH_SHORT).show();
                            else {
                                boolean exist = false;
                                BeaconRegion beaconRegion;
                                if (UUIDEditText.isEnabled()) {
                                    StringBuilder stringBuilder = new StringBuilder(UUIDEditText.getText().toString());
                                    stringBuilder.insert(8,"-");
                                    stringBuilder.insert(13,"-");
                                    stringBuilder.insert(18,"-");
                                    stringBuilder.insert(23,"-");
                                    String UUID = stringBuilder.toString();
                                    if(setDefault[0] && !UUIDList.isEmpty()) UUIDList.set(0, UUID);
                                    else UUIDList.add(UUID);
                                    activityFunctions.resetUUID(view.getContext());

                                    BeaconMonitoring.StopRanging();
                                    BeaconMonitoring.TearDownRanging();
                                    BeaconMonitoring.SetupRanging(view.getContext());
                                    BeaconMonitoring.StartRanging();

                                    beaconRegion = new BeaconRegion(beaconRegionEditText.getText().toString(), java.util.UUID.fromString(UUID), major, minor);
                                    for(int i = 0; i<reminderBeacons.size(); i++){
                                        exist = reminderBeacons.get(i).getMajor().equals(beaconRegion.getMajor())
                                                && reminderBeacons.get(i).getMinor().equals(beaconRegion.getMinor())
                                                && reminderBeacons.get(i).getProximityUUID().equals(beaconRegion.getProximityUUID());
                                    }
                                    if(!exist) {
                                        reminderBeacons.add(beaconRegion);
                                        activityFunctions.resetBeacons(view.getContext());
                                        ChooseReminderBeacon(view);
                                    } else Snackbar.make(view, "BEACON ALREADY EXIST", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    beaconRegion = new BeaconRegion(beaconRegionEditText.getText().toString(), java.util.UUID.fromString(UUID), major, minor);
                                    for(int i = 0; i<reminderBeacons.size(); i++){
                                        exist = reminderBeacons.get(i).getMajor().equals(beaconRegion.getMajor())
                                                && reminderBeacons.get(i).getMinor().equals(beaconRegion.getMinor())
                                                && reminderBeacons.get(i).getProximityUUID().equals(beaconRegion.getProximityUUID());
                                    }
                                    if(!exist){
                                        reminderBeacons.add(beaconRegion);
                                        activityFunctions.resetBeacons(view.getContext());
                                        ChooseReminderBeacon(view);
                                    } else Snackbar.make(view, "BEACON ALREADY EXIST", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else Snackbar.make(view,"EMPTY BEACON PARAMETER(S)!", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(removeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!reminderBeacons.isEmpty()) {
                            removeBuilder.create().show();
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ChooseReminderBeacon(view);
                    }
                });

        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Pick your BEACON")
                .setSingleChoiceItems(adapter, -1, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!adapter.getBeacon().equals("0")){
                            reminder.setBeaconRegion(adapter.getBeacon());
                            int delete = -1;
                            for(int i = 0; i<availableBeacons.size(); i++){
                                if(availableBeacons.get(i).getIdentifier().equals(reminder.getBeaconRegion())) {
                                    delete = i;
                                    break;
                                }
                            }
                            if(delete != -1) availableBeacons.remove(delete);
                            beaconTextView.setText(reminder.getBeaconRegion());
                            adapter.setBeacon("0");
                        } else Snackbar.make(view, "NO BEACON HAS BEEN CHOSEN", Snackbar.LENGTH_SHORT).show();
                        if(activeReminderList.isEmpty()) {
                            stopService(intentService);
                            availableBeacons.clear();
                        }
                        movedIn = false;
                    }
                })
                .setNeutralButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addBuilder.create().show();
                        if(activeReminderList.isEmpty()) {
                            stopService(intentService);
                            availableBeacons.clear();
                            movedIn = false;
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!adapter.getBeacon().equals("0")) adapter.setBeacon("0");
                        if(activeReminderList.isEmpty()) {
                            stopService(intentService);
                            availableBeacons.clear();
                            movedIn = false;
                        }
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            locationTextView.setText(address);
            progressBar.setVisibility(View.GONE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            reminder.setImage(currentPhotoPath);
        }
    }

    public void makeReminderPhoto(View view) {
        addImageView.setVisibility(View.VISIBLE);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(this,R.anim.imagebutton_click_animation));
        activityFunctions.dispatchTakePictureIntent(view.getContext(), reminder);
    }
}
