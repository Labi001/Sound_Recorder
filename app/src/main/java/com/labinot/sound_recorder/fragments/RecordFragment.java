package com.labinot.sound_recorder.fragments;

import static com.labinot.sound_recorder.service.Recording_Service.SOUND_RECORDER_SEP;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.service.Recording_Service;
import com.labinot.sound_recorder.database.DB_Helper;
import com.labinot.sound_recorder.helper.MySharedPreferences;
import com.visualizer.amplitude.AudioRecordView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class RecordFragment extends Fragment {

    public static final int GRANTED = 0;
    public static final int DENIED = 1;
    public static final int BLOCKED_OR_NEVER_ASKED = 2;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static Handler handler = new Handler();
    public String[] mPermissions;
    private AppCompatActivity appCompatActivity;
    private static ExtendedFloatingActionButton mRecord_btn = null;
    private static int mRecordPromptCount = 0;
    private static TextView record_status_txt;
    private static AudioRecordView audioRecordView;
   static Chronometer chronometer = null;
    private int recordCount_txt = 0;
    public static boolean Start_Recording = true;
    private static boolean isRecord_Started = false;
    private DB_Helper mHelper;

    public RecordFragment() {
    }

    public RecordFragment(AppCompatActivity appCompatActivity){
        this.appCompatActivity = appCompatActivity;


    }

    public boolean isRecordStarted(){

        return isRecord_Started;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            mPermissions = new String[]{
                    Manifest.permission.RECORD_AUDIO
            };

        } else {

            mPermissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }



    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mHelper = new DB_Helper(appCompatActivity);
        View record_view = inflater.inflate(R.layout.fragment_record,container,false);

        mRecord_btn = record_view.findViewById(R.id.e_fab_btn);
        record_status_txt = record_view.findViewById(R.id.record_status_text);
        chronometer = record_view.findViewById(R.id.chronometer);
        audioRecordView = record_view.findViewById(R.id.audioRecordView);

         mRecord_btn.shrink();
        mRecord_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (PermissionsAllChecker()){

                    case GRANTED:
                      OnRecord(appCompatActivity,Start_Recording);
                       Start_Recording = !Start_Recording;
                        break;

                    case DENIED:
                       CheckPermission();
                        break;

                    case BLOCKED_OR_NEVER_ASKED:
                         PermissionsBlocked();
                        break;

                    default:
                        break;


                }

            }
        });

       Recording_Service.setMyServiceListener(new Recording_Service.MyServiceListener() {
           @Override
           public void onResult(int response) {

               audioRecordView.update(response);
           }
       });

        return record_view;
    }




    public static void OnRecord(AppCompatActivity appCompatActivity,boolean start) {

        Intent intent = new Intent(appCompatActivity, Recording_Service.class);

        if(start){

            isRecord_Started = true;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecord_btn.extend();
                    mRecord_btn.setIconResource(R.drawable.ic_media_stop);

                }
            },150);

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){

                File folder = new File(Environment.getExternalStorageDirectory() + SOUND_RECORDER_SEP);

                if(!folder.exists())
                    folder.mkdir();

            }

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {

                    switch (mRecordPromptCount){

                        case 0:
                            record_status_txt.setText(appCompatActivity.getString(R.string.recording) + ".");
                            break;

                        case 1:
                            record_status_txt.setText(appCompatActivity.getString(R.string.recording) + "..");
                            break;

                        case 2:
                            record_status_txt.setText(appCompatActivity.getString(R.string.recording) + "...");
                            mRecordPromptCount = -1;
                            break;

                        default:
                            break;

                    }

                    mRecordPromptCount ++;

                }
            });

            appCompatActivity.startService(intent);

            record_status_txt.setText(appCompatActivity.getString(R.string.recording) + ".");
            mRecordPromptCount ++;

        }else{

            audioRecordView.recreate();

            isRecord_Started = false;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecord_btn.shrink();
                    mRecord_btn.setIconResource(R.drawable.ic_mic);

                }
            },150);

            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());
            record_status_txt.setText(appCompatActivity.getString(R.string.tap_the_button));

            appCompatActivity.stopService(intent);
        }
    }



    private int PermissionsAllChecker() {

        if(MySharedPreferences.isFirstTimeAskingPermission(appCompatActivity)){

            MySharedPreferences.FirstTimeAskingPermission(appCompatActivity,false);

            for(String permissions: mPermissions){

                if(ActivityCompat.checkSelfPermission(appCompatActivity,permissions) != PackageManager.PERMISSION_GRANTED)
                    return DENIED;

            }


        }else {

            for(String permissions:mPermissions){

                if(ActivityCompat.checkSelfPermission(appCompatActivity,permissions) != PackageManager.PERMISSION_GRANTED){

                    if(shouldShowRequestPermissionRationale(permissions))
                        return DENIED;
                        else
                            return BLOCKED_OR_NEVER_ASKED;

                }

            }


        }

        return GRANTED;
    }


    private void PermissionsBlocked() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(appCompatActivity);

        dialog.setTitle("Permissions are blocked!");
        dialog.setMessage("Hello you have permanently blocked your permissions please go into your Settings > Apps > Sound Recorder > Permissions to enable them manually.");
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    public void CheckPermission() {

        int result;

        List<String> listPermissionNeeded = new ArrayList<>();

        for(String permission: mPermissions){

            result = ContextCompat.checkSelfPermission(appCompatActivity,permission);

            if(result != PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(permission);

        }

        if(!listPermissionNeeded.isEmpty()){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                appCompatActivity.requestPermissions(listPermissionNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }


        }


    }


    public void stopService() {
        OnRecord(appCompatActivity,false);
    }
}



