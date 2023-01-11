package com.labinot.sound_recorder.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

      Toolbar toolbar =findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      toolbar.setNavigationIcon(R.drawable.ic_back);
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              finish();
          }
      });

        SettingsFragment settingsFragment = new SettingsFragment();
       settingsFragment.setActivity(SettingsActivity.this);
       getSupportFragmentManager().beginTransaction().replace(R.id.container_layout,settingsFragment).commit();


    }
}