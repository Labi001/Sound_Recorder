package com.labinot.sound_recorder.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MySharedPreferences {

    private static final String MY_PREFS = "my_prefs";
    private static final String FIRST_TIME_PERMISSION_ASK = "first_time_permissions_ask";
    private static final String PREF_HIGH_QUALITY = "pref_high_quality";

    public static boolean isFirstTimeAskingPermission(Context context){

        return context.getSharedPreferences(MY_PREFS,Context.MODE_PRIVATE).getBoolean(FIRST_TIME_PERMISSION_ASK,true);
    }

   public static void FirstTimeAskingPermission(Context context,boolean isFirsTime){

       SharedPreferences sharedPreferences = context.getSharedPreferences(MY_PREFS,Context.MODE_PRIVATE);
       SharedPreferences.Editor editor = sharedPreferences.edit();
       editor.putBoolean(FIRST_TIME_PERMISSION_ASK,isFirsTime);
       editor.apply();


   }

    public static void setPrefHighQuality(Context context, boolean isEnabled) {

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(PREF_HIGH_QUALITY,isEnabled);
        editor.apply();


    }

    public static boolean getPrefHighQuality(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_HIGH_QUALITY,true);

    }

}
