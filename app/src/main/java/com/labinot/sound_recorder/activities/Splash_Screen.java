package com.labinot.sound_recorder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.labinot.sound_recorder.R;

public class Splash_Screen extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Splash_ScreenTheme);
        super.onCreate(savedInstanceState);

                startActivity(new Intent(this,MainActivity.class));


        finish();


    }
}