package com.labinot.sound_recorder.listener;

import java.util.List;

public interface UpdateListener {

    void updates(String progress);

    void finish();

    void finish(List<String> deletedFiles);

}
