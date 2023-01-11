package com.labinot.sound_recorder.helper;

import android.os.Parcel;
import android.os.Parcelable;

public class RecordingItem implements Parcelable {

    private int Id;
    private String Name;
    private String FilePAth;
    private int Length;
    private long time;

    public RecordingItem() {
    }

    public RecordingItem(Parcel in) {
        Id = in.readInt();
        Name = in.readString();
        FilePAth = in.readString();
        Length = in.readInt();
        time = in.readLong();
    }

    public static final Creator<RecordingItem> CREATOR = new Creator<RecordingItem>() {
        @Override
        public RecordingItem createFromParcel(Parcel in) {
            return new RecordingItem(in);
        }

        @Override
        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFilePAth() {
        return FilePAth;
    }

    public void setFilePAth(String filePAth) {
        FilePAth = filePAth;
    }

    public int getLength() {
        return Length;
    }

    public void setLength(int length) {
        Length = length;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(Name);
        dest.writeString(FilePAth);
        dest.writeInt(Length);
        dest.writeLong(time);
    }
}
