package com.example.fixap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_animation_layout);

        int SPLASH_TIME_OUT = 3000;

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.image_fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.image_fade_out);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(fadeIn);
        fadeOut.setStartOffset(fadeIn.getDuration() + 1000);
        animationSet.addAnimation(fadeOut);
        animationSet.setFillAfter(true);

        ImageView startAnimationImageView = findViewById(R.id.startAnimationImageView);
        startAnimationImageView.setImageResource(R.drawable.pp_logo_large);
        startAnimationImageView.startAnimation(animationSet);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
