package com.labinot.sound_recorder.adapters;

import static com.labinot.sound_recorder.service.Recording_Service.SOUND_RECORDER_SEP;
import static com.labinot.sound_recorder.service.Recording_Service.getRealPathFromUri;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.background.DeleteAll;
import com.labinot.sound_recorder.database.DB_Helper;
import com.labinot.sound_recorder.database.OnDatabaseChangedListener;
import com.labinot.sound_recorder.fragments.FileViewerFragment;
import com.labinot.sound_recorder.fragments.PlayBackFragment;
import com.labinot.sound_recorder.helper.RecordingItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.ViewHolder> implements OnDatabaseChangedListener {

    public static final String DIALOG_PLAYBACK = "dialog_playback";
    private static final String LOG_TAG = "FileViewerAdapterLog";
    private AppCompatActivity appCompatActivity;
    FileViewerFragment fileViewerFragment;
    LinearLayoutManager linearLayoutManager;
    private DB_Helper mDatabase;
    int selectedIndex =-1;
    private final SparseBooleanArray selectedItems;
    private boolean isSelectModeOn = false;

    public boolean isSelectModeOn() {
        return isSelectModeOn;
    }

    public void setSelectModeOn(boolean selectModeOn) {
        isSelectModeOn = selectModeOn;
    }



    public FileViewerAdapter(AppCompatActivity appCompatActivity, FileViewerFragment fileViewerFragment, LinearLayoutManager linearLayoutManager) {

        this.appCompatActivity = appCompatActivity;
        this.fileViewerFragment = fileViewerFragment;
        this.linearLayoutManager = linearLayoutManager;

        mDatabase = new DB_Helper(appCompatActivity);
           selectedItems = new SparseBooleanArray();
        DB_Helper.setOnDatabaseChangedListener(this);
    }

    @NonNull
    @Override
    public FileViewerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.records_view,parent,false);

        return new ViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull FileViewerAdapter.ViewHolder holder,  int position) {

        RecordingItem recordingItem = getItem(position);

        holder.name.setText(recordingItem.getName());

        long itemDuration = recordingItem.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);
        holder.length.setText(String.format("%02d:%02d",minutes,seconds));

        holder.time_date.setText(DateUtils.formatDateTime(appCompatActivity,
                recordingItem.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME| DateUtils.FORMAT_SHOW_YEAR));

        holder.cardView.setActivated(selectedItems.get(position,false));

        holder.cardView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(isSelectModeOn){

                    toggleSelection(position);

                }else{

                    PlayBackFragment playBackFragment = new PlayBackFragment().newInstance(getItem(holder.getLayoutPosition()),appCompatActivity);
                    FragmentTransaction transaction = appCompatActivity.getSupportFragmentManager().beginTransaction();
                    playBackFragment.show(transaction, DIALOG_PLAYBACK);

                }


            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ArrayList<String> entries = new ArrayList<>();
                entries.add(appCompatActivity.getString(R.string.share));
                entries.add(appCompatActivity.getString(R.string.rename));
                entries.add(appCompatActivity.getString(R.string.delete));

                final CharSequence[] items = entries.toArray(new CharSequence[entries.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
                builder.setTitle(R.string.options);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which){

                            case 0:
                                shareFileDialog(holder.getLayoutPosition());
                                break;

                            case 1:
                                renameFileDialog(holder.getLayoutPosition());
                                break;

                            case 2:
                                deleteFileDialog(holder.getLayoutPosition());
                                break;

                            default:
                                break;

                        }

                    }
                });
                builder.setCancelable(true);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return false;
            }
        });

               toggleIcon(holder.imageView,position);
    }

    private void toggleIcon(ImageView image, int position) {

        if(selectedItems.get(position,false))

            image.setImageResource(R.drawable.ic_check);
        else
            image.setImageResource(R.drawable.ic_mic);

        if(selectedIndex == position)
            selectedIndex = -1;


    }

    private void toggleSelection(int position) {

        selectedIndex = position;

        if(selectedItems.get(position,false))

            selectedItems.delete(position);
        else
            selectedItems.put(position,true);

        notifyItemChanged(position);

        int count = selectedItemCount();
        fileViewerFragment.actionMode.setTitle(appCompatActivity.getString(R.string.select,count));
        fileViewerFragment.actionMode.invalidate();



    }

    private int selectedItemCount() {

        return selectedItems.size();
    }

    public void clearSelection(){

        selectedItems.clear();
        notifyDataSetChanged();

    }

    private void deleteFileDialog(int position) {

        AlertDialog.Builder deleteFileBuilder = new AlertDialog.Builder(appCompatActivity);
        deleteFileBuilder.setTitle(R.string.confirm_delete);
        deleteFileBuilder.setMessage(R.string.delete_message);
        deleteFileBuilder.setCancelable(true);
        deleteFileBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removes(position);
            }
        });

        deleteFileBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = deleteFileBuilder.create();
        alertDialog.show();
    }

    public List<Integer> getSelectedItems() {

        List<Integer> items = new ArrayList<>(selectedItems.size());

        for(int i=0; i < selectedItems.size(); i++){

            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    private void removes(int position) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            final String where = MediaStore.MediaColumns.DISPLAY_NAME + "=?";

            final String[] selectionArguments = new String[]{

                    getItem(position).getName()
            };

            final ContentResolver contentResolver = appCompatActivity.getContentResolver();
            final Uri mFileURI = MediaStore.Files.getContentUri("external");

            contentResolver.delete(mFileURI,where,selectionArguments);

        }else{

            File deleteFile = new File(getItem(position).getFilePAth());

            if(deleteFile.delete())
                Log.d(LOG_TAG, "File Deleted :" + deleteFile.getAbsolutePath());
            else
                Log.d(LOG_TAG, "File Not Deleted :" + deleteFile.getAbsolutePath());



        }

        mDatabase.removeItemWithID(getItem(position).getId());
        notifyItemRemoved(position);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                fileViewerFragment.checkData();
            }
        },300);

    }

    private void shareFileDialog(int position) {

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM,shareFile(position));
        shareIntent.setType("audio/mp4");
        appCompatActivity.startActivity(Intent.createChooser(shareIntent,appCompatActivity.getString(R.string.send_to)));

    }

    private void renameFileDialog(int position) {

        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(appCompatActivity);
        LayoutInflater inflater = LayoutInflater.from(appCompatActivity);

        View view = inflater.inflate(R.layout.dialog_rename_file,null);

        final EditText input = view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(appCompatActivity.getString(R.string.rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(appCompatActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(input.getText().toString().trim().isEmpty())
                    return;

                String value = input.getText().toString().trim()+appCompatActivity.getString(R.string.mp3);
                Rename(position,value);

                dialog.cancel();

            }
        });

        renameFileBuilder.setNegativeButton(appCompatActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        renameFileBuilder.setView(view);
        AlertDialog alertDialog = renameFileBuilder.create();
        alertDialog.show();

    }

    public boolean removeOutofApp() {

        if(mDatabase.getCount()>0){
            DeleteAll deleteAll = new DeleteAll(appCompatActivity,this,mDatabase);
            deleteAll.execute();
            return true;
        }else{

            return false;
        }

    }


    @SuppressLint("StringFormatMatches")
    private void Rename(int position, String name) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentResolver contentResolver = appCompatActivity.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME,name);

            Uri uri = getRealPathFromUri(appCompatActivity,getItem(position).getName());
            contentResolver.update(uri,contentValues,null,null);

            mDatabase.renameItem(getItem(position),name,String.valueOf(uri));
            notifyItemChanged(position);

        } else {

            String mFilePath;
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath +=SOUND_RECORDER_SEP + "/" + name;
            File file = new File(mFilePath);

            if(file.exists() && !file.isDirectory()){

                Toast.makeText(appCompatActivity, String.format(appCompatActivity.getString(R.string.toast_file_exists),name), Toast.LENGTH_SHORT).show();
            }else{

                File oldFilePath = new File(getItem(position).getFilePAth());
                oldFilePath.renameTo(file);
                mDatabase.renameItem(getItem(position),name,mFilePath);
                notifyItemChanged(position);

            }

        }

    }

    private Uri shareFile(int position) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getRealPathFromUri(appCompatActivity,getItem(position).getName());
        } else {
            return Uri.fromFile(new File(getItem(position).getFilePAth()));
        }


    }

    private RecordingItem getItem(int position) {

        return mDatabase.getItemAt(position);

    }

    @Override
    public int getItemCount() {

        return mDatabase.getCount();
    }

    @Override
    public void onNewDatabaseEntryAdded() {

        notifyItemInserted(getItemCount() -1);
        linearLayoutManager.scrollToPosition(getItemCount() -1);
    }

    @Override
    public void onDatabaseEntryRenamed() {

    }

    public void checkIfFileIsDeletedOutside(List<String> deletedFiles) {

        if(deletedFiles.size()==0){

            if(mDatabase.getCount() > 0) {
                mDatabase.deleteAllData();
                notifyDataSetChanged();
                Log.d(LOG_TAG, "No File Exist Delete Database");
            }else{
                Log.d(LOG_TAG, "Everything is Clear");

            }


        }else{

            List<Integer> needToBeDeleted = new ArrayList<>();

            for(int i=0; i < mDatabase.getCount(); i++){

                if(mDatabase.getItemAt(i) != null){

                    if(!deletedFiles.contains(mDatabase.getItemAt(i).getName())) {
                        Log.d(LOG_TAG, "File Doesn't Exist More - " + mDatabase.getItemAt(i).getName());
                        needToBeDeleted.add(mDatabase.getItemAt(i).getId());
                    }

                }

            }

            for (int id:needToBeDeleted){

                  mDatabase.removeItemWithID(id);
            }

        }

    }

    public void removeItems(int position) {

        removes(position);
        selectedIndex = -1;

    }


    public class ViewHolder extends RecyclerView.ViewHolder {

          private TextView name,length,time_date;
          private ImageView imageView;
          private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.file_name_text);
            length = itemView.findViewById(R.id.file_length_text);
            time_date = itemView.findViewById(R.id.file_date_added_text);
            imageView = itemView.findViewById(R.id.imageView);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }

}
