package com.example.fixap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.Objects;

import static com.example.fixap.BeaconApplication.background;
import static com.example.fixap.MainActivity.activityFunctions;

public class PhotoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFunctions.hideStatusBar(this,null);
        setContentView(R.layout.photo_layout);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ImageView imageView = findViewById(R.id.reminder_photo);

        Intent intent = getIntent();
        Reminder reminder = intent.getParcelableExtra("reminderImage");

        Bitmap reminderImage = decodePic(reminder.getImage());
        imageView.setImageBitmap(reminderImage);
    }

    private Bitmap decodePic(String pPath){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pPath);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pPath);
    }

    @Override
    protected void onResume() {
        super.onResume();
        background = false;
        activityFunctions.hideStatusBar(this,null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        background = true;
        activityFunctions.hideStatusBar(this,null);
    }
}
