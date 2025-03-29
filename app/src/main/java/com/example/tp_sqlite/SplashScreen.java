package com.example.tp_sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {
    private ImageView loadingImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        loadingImg = findViewById(R.id.loadingImg);
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, 360,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
        );

        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        rotateAnimation.setRepeatMode(RotateAnimation.RESTART);

        loadingImg.startAnimation(rotateAnimation);

        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000);
                    Intent intent  = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        t.start();
    }
}