package com.labinot.sound_recorder.database;

import static com.labinot.sound_recorder.database.DB_Helper.DB_HelperItem.COLUMN_NAME_RECORDING_FILE_PATH;
import static com.labinot.sound_recorder.database.DB_Helper.DB_HelperItem.COLUMN_NAME_RECORDING_LENGTH;
import static com.labinot.sound_recorder.database.DB_Helper.DB_HelperItem.COLUMN_NAME_RECORDING_NAME;
import static com.labinot.sound_recorder.database.DB_Helper.DB_HelperItem.COLUMN_NAME_RECORDING_TIME_ADDED;
import static com.labinot.sound_recorder.database.DB_Helper.DB_HelperItem.TABLE_NAME;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import com.labinot.sound_recorder.helper.RecordingItem;

public class DB_Helper extends SQLiteOpenHelper {

    private Context context;
    private static OnDatabaseChangedListener onDatabaseChangedListener;
    public static final String DATABASE_NAME = "saved_recordings.db";
    public static final int DATABASE_VERSION = 1;
    public static final String COMMA_SEP = ",";
    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    private static final String SQL_CREATE_ENTRIES =

            "CREATE TABLE " + DB_HelperItem.TABLE_NAME + " (" +

                    DB_HelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_RECORDING_LENGTH + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_RECORDING_TIME_ADDED + INTEGER_TYPE + ")";

    public DB_Helper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
          db.execSQL(SQL_CREATE_ENTRIES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener){

        onDatabaseChangedListener = listener;

    }



    @SuppressLint("Range")
    public RecordingItem getItemAt(int position) {

        SQLiteDatabase sql = getReadableDatabase();

        String[] projection = {DB_HelperItem._ID,
        COLUMN_NAME_RECORDING_NAME,
        COLUMN_NAME_RECORDING_FILE_PATH,
        COLUMN_NAME_RECORDING_LENGTH,
        COLUMN_NAME_RECORDING_TIME_ADDED};

        Cursor c = sql.query(TABLE_NAME,projection,null,null,null,null,null);

        if(c.moveToPosition(position)){

            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(DB_HelperItem._ID)));
            item.setName(c.getString(c.getColumnIndex(COLUMN_NAME_RECORDING_NAME)));
            item.setFilePAth(c.getString(c.getColumnIndex(COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setLength(c.getInt(c.getColumnIndex(COLUMN_NAME_RECORDING_LENGTH)));
            item.setTime(c.getLong(c.getColumnIndex(COLUMN_NAME_RECORDING_TIME_ADDED)));
            c.close();

            return item;

        }

        return null;

    }

    public long addRecording(String recordingName,String file_path,long length){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME_RECORDING_NAME, recordingName);
        contentValues.put(COLUMN_NAME_RECORDING_FILE_PATH, file_path);
        contentValues.put(COLUMN_NAME_RECORDING_LENGTH, length);
        contentValues.put(COLUMN_NAME_RECORDING_TIME_ADDED, System.currentTimeMillis());

        long rowID= sqLiteDatabase.insert(DB_HelperItem.TABLE_NAME,null,contentValues);

        if(onDatabaseChangedListener != null)
            onDatabaseChangedListener.onNewDatabaseEntryAdded();

        return rowID;

    }

    public int getCount() {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String[] projection = {DB_HelperItem._ID};

        Cursor c = sqLiteDatabase.query(DB_HelperItem.TABLE_NAME,projection,null,null,null,null,null);

        int count = c.getCount();
        c.close();

        return count;

    }

    public void renameItem(RecordingItem item, String name, String filePath) {

        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_RECORDING_NAME,name);
        contentValues.put(COLUMN_NAME_RECORDING_FILE_PATH,filePath);

        db.update(TABLE_NAME,contentValues,DB_HelperItem._ID + "=" + item.getId(),null);

        if(onDatabaseChangedListener!=null)
            onDatabaseChangedListener.onDatabaseEntryRenamed();


    }

    public void removeItemWithID(int id) {

        SQLiteDatabase db = getWritableDatabase();
        String[] whereArg ={String.valueOf(id)};
        db.delete(TABLE_NAME,"_ID=?",whereArg);

    }

    public void deleteAllData() {

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " +TABLE_NAME);
    }

    public static abstract class DB_HelperItem implements BaseColumns {

        public static final String TABLE_NAME = "saved_recordings";
        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_RECORDING_TIME_ADDED = "time_added";

    }

}
