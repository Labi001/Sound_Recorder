package com.labinot.sound_recorder.database;

public interface OnDatabaseChangedListener {

    void onNewDatabaseEntryAdded();

    void onDatabaseEntryRenamed();
}
