package com.labinot.sound_recorder.service;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RestrictTo;
import androidx.core.app.NotificationCompat;

import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.activities.MainActivity;
import com.labinot.sound_recorder.database.DB_Helper;
import com.labinot.sound_recorder.helper.MySharedPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Recording_Service extends Service {

    public static final String SOUND_RECORDER_ID = "SoundRecorderID";
    public static final String SOUND_RECORDER_SEP = "/SoundRecorder";
    public static final String SOUND_RECORDER = "SoundRecorder";
    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private MediaRecorder mRecorder = null;
    private String mFilePath;
    private String mFileName = null;
    private ContentResolver contentResolver;
    private ContentValues contentValues;
    private Uri mFileUri;
    private ParcelFileDescriptor file;
    private DB_Helper database;
    private Timer mTimer;
    private TimerTask mIncrementTimerTask;
    private Timer mTimerVisual;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private long mElapsedSeconds = 0;
    private static final String LOG_TAG = "RecordingServiceLog";


    private static MyServiceListener myServiceListener;

    public static MyServiceListener getMyServiceListener() {
        return Recording_Service.myServiceListener;
    }

    public static void setMyServiceListener(MyServiceListener myServiceListener) {
        Recording_Service.myServiceListener = myServiceListener;
    }

    public interface MyServiceListener {

        void onResult(int response);
    }

    public Recording_Service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startRecording();

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        database = new DB_Helper(getApplicationContext());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        if(mRecorder != null)
            stopRecording();

        super.onTaskRemoved(rootIntent);
    }

    private void startRecording() {

        setFileNameandPath();

        mRecorder = new MediaRecorder();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            contentResolver = getContentResolver();
            contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Media.DATE_ADDED,System.currentTimeMillis()/1000);
            contentValues.put(MediaStore.Audio.Media.MIME_TYPE,"audio/mp3");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_MUSIC+ File.separator+SOUND_RECORDER);
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME,mFileName);

            mFileUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,contentValues);

            mFilePath = String.valueOf(getRealPathFromUri(this,mFileName));

            try {
                file = contentResolver.openFileDescriptor(mFileUri,"w");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            mRecorder.setOutputFile(file.getFileDescriptor());


        }else{

            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath = mFilePath + SOUND_RECORDER_SEP + "/" + mFileName;
            mRecorder.setOutputFile(mFilePath);

        }


        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);

        if(MySharedPreferences.getPrefHighQuality(this)){

            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(192000);
        }


        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            voiceVisual();
            startTimer();
            startForeground(1,createNotification());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startTimer() {

         mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {

                mElapsedSeconds++;

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1,createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask,1000,1000);

    }

    private void voiceVisual() {

        mTimerVisual = new Timer();
        mTimerVisual.schedule(new TimerTask() {
            @Override
            public void run() {

                if(getMyServiceListener() != null && mRecorder!=null)
                    getMyServiceListener().onResult(mRecorder.getMaxAmplitude());

            }
        },0,100);

    }

    @SuppressLint("WrongConstant")
    private Notification createNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNoBuilder = new NotificationCompat.Builder(this,SOUND_RECORDER_ID);

        NotificationChannel notificationChannel;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationChannel = new NotificationChannel(SOUND_RECORDER_ID,SOUND_RECORDER,NotificationManager.IMPORTANCE_NONE);

            if(notificationManager != null && notificationManager.getNotificationChannel(SOUND_RECORDER_ID) == null)
                notificationManager.createNotificationChannel(notificationChannel);

            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationChannel.setSound(null,null);
            notificationChannel.setShowBadge(false);

        }

        mNoBuilder.setContentTitle(getString(R.string.notification_recording));
        mNoBuilder.setContentText(mTimerFormat.format(mElapsedSeconds * 1000));
        mNoBuilder.setOngoing(true);
        mNoBuilder.setSound(null);
        mNoBuilder.setVibrate(null);
        mNoBuilder.setSmallIcon(R.drawable.ic_mic);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        mNoBuilder.setContentIntent(PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_MUTABLE));
        else
            mNoBuilder.setContentIntent(PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT));


        return mNoBuilder.build();
    }

    @SuppressLint("Range")
    public static Uri getRealPathFromUri(Context context, String Name) {

        String mAudioFileId = "";

        String[] projection = new String[]{

                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
        };

        String selection = MediaStore.Audio.Media.DISPLAY_NAME + "=?";

        final String[] selectionArguments = new String[]{

                Name
        };

        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        Uri collection;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)

            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        else
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        try(

                Cursor cursor = context.getContentResolver().query(
                        collection,
                        projection,
                        selection,
                        selectionArguments,
                        sortOrder

                )

                ) {

            if(cursor !=null){

                while (cursor.moveToNext()){

                    mAudioFileId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                }

            }

        }

        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,"" + mAudioFileId);

    }

    private void setFileNameandPath() {

        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd MM:mm:ss");
        String str_date = dateFormat.format(date);
        str_date = str_date.replaceAll("[^a-zA-Z0-9]","");

        mFileName = "My Recording" + "_"+str_date+".mp3";

    }

    public void stopRecording(){

        if(mTimerVisual != null){

            mTimerVisual.cancel();
            mTimerVisual = null;
        }

        try{

            mRecorder.stop();
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
            mRecorder.release();

            Toast.makeText(this, "Recording saved to: " + mFilePath, Toast.LENGTH_SHORT).show();

            if(mIncrementTimerTask != null){

                mIncrementTimerTask.cancel();
               mIncrementTimerTask = null;
            }

            mRecorder = null;

            try{
                database.addRecording(mFileName,mFilePath,mElapsedMillis);

            }catch (Exception e){
                Log.e(LOG_TAG, "Exception ", e);
            }

        } catch (RuntimeException e) {
        Log.e(LOG_TAG, "RuntimeException ", e);
        }

        stopForeground(true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }
}