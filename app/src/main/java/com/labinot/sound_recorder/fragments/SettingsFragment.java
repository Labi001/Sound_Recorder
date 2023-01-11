package com.labinot.sound_recorder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.labinot.sound_recorder.BuildConfig;
import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.about.About;
import com.labinot.sound_recorder.activities.SettingsActivity;
import com.labinot.sound_recorder.helper.MySharedPreferences;

public class SettingsFragment extends PreferenceFragmentCompat {

    private AppCompatActivity mappCompatActivity;

    public void setActivity(AppCompatActivity appCompatActivity) {

        this.mappCompatActivity = appCompatActivity;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        setPreferencesFromResource(R.xml.preferences,rootKey);

        CheckBoxPreference highQualityPref = findPreference(getString(R.string.pref_high_quality_key));
        highQualityPref.setChecked(MySharedPreferences.getPrefHighQuality(mappCompatActivity));
        highQualityPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {

                MySharedPreferences.setPrefHighQuality(mappCompatActivity,(boolean) newValue);

                return true;
            }
        });

        Preference aboutPref = findPreference(getString(R.string.pref_about_key));
        aboutPref.setSummary(getString(R.string.pref_about_desc, BuildConfig.VERSION_NAME));
        aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {

                startActivity(new Intent(mappCompatActivity, About.class));
                return true;
            }
        });

    }


}
