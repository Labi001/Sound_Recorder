package com.labinot.sound_recorder.activities;

import static com.labinot.sound_recorder.fragments.RecordFragment.REQUEST_ID_MULTIPLE_PERMISSIONS;
import static com.labinot.sound_recorder.fragments.RecordFragment.Start_Recording;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.adapters.MyViewPagerAdapter;
import com.labinot.sound_recorder.fragments.FileViewerFragment;
import com.labinot.sound_recorder.fragments.RecordFragment;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private String[] title;
    FileViewerFragment fileViewerFragment;
    RecordFragment mRecordFragment;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(mRecordFragment.isRecordStarted())
            mRecordFragment.stopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lifeOfApp();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        title = new String[]{"Record","Saved Recordings"};



        MyViewPagerAdapter viewPagerAdapter = new MyViewPagerAdapter(this);


        viewPagerAdapter.addFragment(new RecordFragment(this));
        viewPagerAdapter.addFragment(new FileViewerFragment(this));

        viewPager.setAdapter(viewPagerAdapter);
        mRecordFragment = (RecordFragment) viewPagerAdapter.getFragment(0);
        fileViewerFragment = (FileViewerFragment) viewPagerAdapter.getFragment(1);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                tab.setText(title[position]);
            }
        }).attach();

    }

    private void lifeOfApp() {

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {

                switch (event){

                    case ON_START:
                        break;

                    case ON_CREATE:
                        break;

                    case ON_RESUME:

                        if (fileViewerFragment != null)
                            fileViewerFragment.trackWhenFileDeleted();

                        break;

                    case ON_STOP:
                        break;

                }

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS){

            int recorderPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);

            if(recorderPermission == PackageManager.PERMISSION_GRANTED){

                RecordFragment.OnRecord(MainActivity.this,Start_Recording);
                Start_Recording = !Start_Recording;


            }else{
                Toast.makeText(this, getString(R.string.denied_permission), Toast.LENGTH_SHORT).show();
            }

        }

    }


}