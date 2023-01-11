package com.labinot.sound_recorder.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.adapters.FileViewerAdapter;
import com.labinot.sound_recorder.background.TrackFileChanges;
import com.labinot.sound_recorder.helper.util;

import java.util.List;
import java.util.Timer;

public class FileViewerFragment extends Fragment {

    private AppCompatActivity appCompatActivity;
    private ConstraintLayout mainLayout;
    private RecyclerView recyclerView;
    private LinearLayout empty_layout;
    private FileViewerAdapter fileViewerAdapter;
    public ActionMode actionMode;
    private ActionCallBack actionCallBack;


    public FileViewerFragment() {
    }

    public FileViewerFragment(AppCompatActivity appCompatActivity){
        this.appCompatActivity = appCompatActivity;

    }

    private void toggleActionBar() {

        if(actionMode == null)
            actionMode = appCompatActivity.startSupportActionMode(actionCallBack);


    }

    private void deleteMultiListSelection(){

        List<Integer> selectedItemPositions = fileViewerAdapter.getSelectedItems();

        for(int i = selectedItemPositions.size() -1; i>=0; i--){

             fileViewerAdapter.removeItems(selectedItemPositions.get(i));
        }
        fileViewerAdapter.notifyDataSetChanged();
    }

    private class ActionCallBack implements ActionMode.Callback{


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            util.toggleStatusBarColor(appCompatActivity,getResources().getColor(R.color.primary));
            mode.getMenuInflater().inflate(R.menu.select_menu,menu);
            mode.setTitle(getString(R.string.select,0));
            fileViewerAdapter.setSelectModeOn(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            if(item.getItemId()==R.id.agree_select){

                deleteMultiListSelection();
                mode.finish();

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            fileViewerAdapter.clearSelection();
            actionMode = null;
            util.toggleStatusBarColor(appCompatActivity,getResources().getColor(R.color.primary_dark));
            fileViewerAdapter.setSelectModeOn(false);

        }
    }

    public void trackWhenFileDeleted(){

        if(fileViewerAdapter != null){

            TrackFileChanges trackFileChanges;
            trackFileChanges = new TrackFileChanges(appCompatActivity,fileViewerAdapter);
            trackFileChanges.execute();

        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.saved_records_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.delete_all:

                if(!fileViewerAdapter.removeOutofApp())
                    noRecordsSnackBar();

                return true;

            case R.id.select_multi_item:

                if(fileViewerAdapter.getItemCount()>0)
                  toggleActionBar();
                else
                    noRecordsSnackBar();
                return true;

        }

        return false;
    }


    private void noRecordsSnackBar() {
        Snackbar snackbar = Snackbar.make(mainLayout,"No records are saved...",Snackbar.LENGTH_SHORT)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                .setBackgroundTint(getResources().getColor(R.color.primary))
                .setTextColor(Color.WHITE);
        snackbar.show();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View fileVF_view = inflater.inflate(R.layout.fragment_file_viewer,container,false);

        mainLayout = fileVF_view.findViewById(R.id.main_layout);
        recyclerView = fileVF_view.findViewById(R.id.recycleView_file);
        empty_layout = fileVF_view.findViewById(R.id.empty_layout);


        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(appCompatActivity);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        actionCallBack = new ActionCallBack();

        fileViewerAdapter = new FileViewerAdapter(appCompatActivity,this,layoutManager);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fileViewerAdapter);

        fileViewerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
               recyclerView.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       checkData();
                   }
               },300);
            }
        });

        checkData();

        return fileVF_view;
    }

    public void checkData() {

        if(fileViewerAdapter != null){

            if(fileViewerAdapter.getItemCount() > 0){

                recyclerView.setVisibility(View.VISIBLE);
                empty_layout.setVisibility(View.GONE);

            }else{

                recyclerView.setVisibility(View.GONE);
                empty_layout.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
      checkData();
    }
}
