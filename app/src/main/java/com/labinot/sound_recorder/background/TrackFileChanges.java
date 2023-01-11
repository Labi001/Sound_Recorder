package com.labinot.sound_recorder.background;



import static com.labinot.sound_recorder.service.Recording_Service.SOUND_RECORDER_SEP;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import com.labinot.sound_recorder.adapters.FileViewerAdapter;
import com.labinot.sound_recorder.listener.UpdateListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrackFileChanges implements UpdateListener {

    private final FileViewerAdapter mFileViewerAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    public final CheckIfDeleted checkIfDeleted;

    public TrackFileChanges(AppCompatActivity appCompatActivity,FileViewerAdapter mFileViewerAdapter) {
        this.mFileViewerAdapter = mFileViewerAdapter;
        this.checkIfDeleted = new CheckIfDeleted(appCompatActivity,this);
    }

  public void execute(){
        checkIfDeleted.doInBackground();
  }


    @Override
    public void updates(String progress) {

    }

    @Override
    public void finish() {

    }

    @Override
    public void finish(List<String> deletedFiles) {

        handler.post(new Runnable() {
            @Override
            public void run() {

                mFileViewerAdapter.checkIfFileIsDeletedOutside(deletedFiles);

            }
        });

    }

    static class CheckIfDeleted {

        public final AppCompatActivity mAppCompatActivity;
        public final UpdateListener updateListener;

        public CheckIfDeleted(AppCompatActivity appCompatActivity,UpdateListener updateListener) {

            this.mAppCompatActivity = appCompatActivity;
            this.updateListener = updateListener;

        }


        public void doInBackground() {

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        deleteFilesQ();
                    else
                        deleteFilesNormal();
                    
                }
            });

        }

        @SuppressLint("Range")
        private void deleteFilesQ() {

            String mFileName;

            List<String> filesNotInStorage = new ArrayList<>();

            String[] projections = new String[]{

                    MediaStore.Audio.Media.DISPLAY_NAME
            };

            Uri collections;

            collections = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

           try (Cursor cursor = mAppCompatActivity.getContentResolver().query(

                   collections,
                   projections,
                   null,
                   null,
                   null

           )){

               if(cursor != null){

                   if(cursor.moveToFirst()){

                       do{

                           mFileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                           filesNotInStorage.add(mFileName);

                       }
                       while (cursor.moveToNext());

                   }
                   cursor.close();

               }

           }

           updateListener.finish(filesNotInStorage);

        }

        private void deleteFilesNormal() {

            List<String> filesNotInStorage = new ArrayList<>();

            File file = new File(Environment.getExternalStorageDirectory() + SOUND_RECORDER_SEP);
            String [] files;

            files = file.list();

            for(String name:files){

              File myFiles = new File(file,name);
              filesNotInStorage.add(myFiles.getName());
            }
            updateListener.finish(filesNotInStorage);

        }

    }
}
