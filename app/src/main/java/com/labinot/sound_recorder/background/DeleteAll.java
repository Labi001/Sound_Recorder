package com.labinot.sound_recorder.background;

import static com.labinot.sound_recorder.service.Recording_Service.SOUND_RECORDER_SEP;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.adapters.FileViewerAdapter;
import com.labinot.sound_recorder.database.DB_Helper;
import com.labinot.sound_recorder.listener.UpdateListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteAll implements UpdateListener {

    private final AppCompatActivity appCompatActivity;
    private final FileViewerAdapter fileViewerAdapter;
    private final DB_Helper database;
    private AlertDialog alertDialog;
    private TextView mProgressTextView;
    private ProgressBar progressBar;
    private final DeleteAllExecutor deleteAllExecutor;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public DeleteAll(AppCompatActivity appCompatActivity, FileViewerAdapter fileViewerAdapter, DB_Helper database) {
        this.appCompatActivity = appCompatActivity;
        this.fileViewerAdapter = fileViewerAdapter;
        this.database = database;
        deleteAllExecutor = new DeleteAllExecutor(appCompatActivity,database,this);
    }

    public void execute() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(appCompatActivity);
        LayoutInflater inflater = LayoutInflater.from(appCompatActivity);
        View dialogView = inflater.inflate(R.layout.alert_dialog_database_delete,null);

        mProgressTextView = dialogView.findViewById(R.id.mProgressTextView);
        progressBar = dialogView.findViewById(R.id.progressBar);

        int maxItems = database.getCount();
        progressBar.setMax(maxItems);

        dialog.setTitle(R.string.please_wait);
        dialog.setMessage(R.string.deleting_all);
        dialog.setView(dialogView);
        alertDialog = dialog.create();
         alertDialog.setCancelable(false);

         if(!appCompatActivity.isFinishing())
             alertDialog.show();

         deleteAllExecutor.doInBackground();

    }

    @Override
    public void updates(String progress) {

        handler.post(new Runnable() {
            @Override
            public void run() {

                if(progressBar != null){

                    mProgressTextView.setText(progress);
                    progressBar.setProgress(Integer.parseInt(progress));

                }

            }
        });


    }

    @Override
    public void finish() {

        handler.post(new Runnable() {
            @Override
            public void run() {

                alertDialog.dismiss();
                fileViewerAdapter.notifyDataSetChanged();

            }
        });

    }

    @Override
    public void finish(List<String> deletedFiles) {

    }

    static class DeleteAllExecutor {

        private final AppCompatActivity appCompatActivity;
        private final DB_Helper mDatabase;
        private final UpdateListener updateListener;
        private final int MaxItems;

        DeleteAllExecutor(AppCompatActivity appCompatActivity, DB_Helper mDatabase, UpdateListener updateListener) {
            this.appCompatActivity = appCompatActivity;
            this.mDatabase = mDatabase;
            this.updateListener = updateListener;

            MaxItems = mDatabase.getCount();
        }

        public void doInBackground(){

            ExecutorService executorService = Executors.newSingleThreadExecutor();

            executorService.execute(new Runnable() {
                @Override
                public void run() {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        deleteFilesQ();
                    else
                        deleteFilesNormal();


                    updateListener.finish();
                }
            });

        }

        @SuppressLint("Range")
        @RequiresApi(api = Build.VERSION_CODES.Q)
        private void deleteFilesQ() {

            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
            ContentProviderOperation contentProviderOperation;

            Uri uri = null;
            String mAudioFileID;

            String [] projection = new String[] {
                    MediaStore.Audio.Media._ID
            };

             String selection = MediaStore.Audio.Media._ID + "=?";

             Uri collection;

             collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

             try(
                     Cursor cursor = appCompatActivity.getContentResolver().query(

                             collection,
                             projection,
                             null,
                             null,
                             null

                     )

                     ){

                  if(cursor != null){

                      if(cursor.moveToFirst()){

                          do{

                              mAudioFileID = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                              uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,"" +mAudioFileID);

                              contentProviderOperation = ContentProviderOperation
                                      .newDelete(uri)
                                      .withSelection(selection,new String[]{mAudioFileID})
                                      .build();

                              operationList.add(contentProviderOperation);
                          }
                          while (cursor.moveToNext());

                      }

                    cursor.close();
                  }

                  if(operationList.size()>0){

                      try{

                          appCompatActivity.getContentResolver().applyBatch(uri.getAuthority(),operationList);
                          mDatabase.deleteAllData();
                      } catch ( OperationApplicationException | RemoteException e) {
                          e.printStackTrace();
                      }

                      for(int i=0; i<= operationList.size(); i++){

                          updateListener.updates(String.valueOf(i));
                      }

                      updateListener.updates(String.valueOf(MaxItems));

                      try {
                        Thread.sleep(250);
                      }catch (InterruptedException e){

                          e.printStackTrace();
                      }

                  }

             }

        }

        private void deleteFilesNormal() {

            File file = new File(Environment.getExternalStorageDirectory() + SOUND_RECORDER_SEP);

            if(file.length() > 0){

                String[] files;
                files = file.list();

                for(int i=0; i < files.length; i++){

                    File myFile = new File(file,files[i]);

                    myFile.delete();

                    updateListener.updates(String.valueOf(i));
                }

                mDatabase.deleteAllData();

                updateListener.updates(String.valueOf(MaxItems));

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


        }
    }
}
