package com.labinot.sound_recorder.fragments;

import static com.labinot.sound_recorder.service.Recording_Service.getRealPathFromUri;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.labinot.sound_recorder.R;
import com.labinot.sound_recorder.helper.RecordingItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class PlayBackFragment extends DialogFragment {


    private static final String ARG_ITEM = "recording_item";
    private static final String LOG_TAG = "PlaybackFragmentLog";
    private final int SEEKBAR_SPEED = 50;

    private RecordingItem item;
    private Context context;
    private long minutes;
    private long seconds;
    private TextView mFileNameTextView;
    private TextView mFileLengthTextView;
    private TextView mCurrentProgressTextView;
    private SeekBar mSeekBar;
    private boolean isPlaying = false;
    private FloatingActionButton mPlayButton;
    private MediaPlayer mediaPlayer;
    private final Handler handler =new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            if(mediaPlayer != null){

                int mCurrentPosition = mediaPlayer.getCurrentPosition();
                mSeekBar.setMax(mediaPlayer.getDuration());
                updateCurrentTime(mCurrentPosition);
                animateSeekBar(SEEKBAR_SPEED,mCurrentPosition);
                updateSeekBar();

            }

        }
    };

    public PlayBackFragment() {

    }


    public PlayBackFragment(Context context) {

        this.context = context;
    }

    public PlayBackFragment newInstance(RecordingItem item, Context context) {
        PlayBackFragment fragment = new PlayBackFragment(context);
        Bundle args = new Bundle();

            args.putParcelable(ARG_ITEM, item);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        item = getArguments().getParcelable(ARG_ITEM);

        long itemDuration = item.getLength();

        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

      Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_play_back, null);

        mFileNameTextView = view.findViewById(R.id.file_name_text_view);
        mFileLengthTextView = view.findViewById(R.id.file_length_text_view);
        mCurrentProgressTextView = view.findViewById(R.id.current_progress_text_view);
        mSeekBar = view.findViewById(R.id.seekbar);
        mPlayButton = view.findViewById(R.id.floatingActionButton);

        ColorFilter filter = new LightingColorFilter(getResources().getColor(R.color.primary), getResources().getColor(R.color.primary));

        mSeekBar.getProgressDrawable().setColorFilter(filter);
        mSeekBar.getThumb().setColorFilter(filter);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Log.d(LOG_TAG, "onProgressChanged - progress: " + progress);
                Log.d(LOG_TAG, "onProgressChanged - fromUser: " + fromUser);

                seekBar.setProgress(progress);
                updateCurrentTime(progress);

                if(mediaPlayer != null && fromUser){

                    mediaPlayer.seekTo(progress);
                    handler.removeCallbacks(mRunnable);

                }else if(mediaPlayer == null && fromUser){

                    prepareMediaPlayerFromPoint(progress);

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                Log.d(LOG_TAG, "onStartTrackingTouch");
                handler.removeCallbacks(mRunnable);


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Log.d(LOG_TAG, "onStopTrackingTouch");

                handler.removeCallbacks(mRunnable);

                if(mediaPlayer != null){

                    mediaPlayer.seekTo(seekBar.getProgress());
                    updateSeekBar();

                }else if(mediaPlayer == null){

                    prepareMediaPlayerFromPoint(seekBar.getProgress());

                }

                updateCurrentTime(seekBar.getProgress());

                boolean reachedEnd = mCurrentProgressTextView.getText().toString().equals(mFileLengthTextView.getText().toString());

                if(reachedEnd){

                    stopPlaying();
                }else {

                    if (!isPlaying)
                        mPlayButton.setImageResource(R.drawable.ic_media_play);
                }

            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onPlay(isPlaying);
                isPlaying = !isPlaying;

            }
        });

        mFileNameTextView.setText(item.getName());
        mFileLengthTextView.setText(String.format("%02d:%02d", minutes, seconds));

        builder.setView(view);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
    }

    private void prepareMediaPlayerFromPoint(int progress) {

        mediaPlayer = new MediaPlayer();

        playAudio(false);

        mediaPlayer.seekTo(progress);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @SuppressLint("DefaultLocale")
    private void updateCurrentTime(int mCurrentProgress) {

        long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentProgress);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentProgress) - TimeUnit.MINUTES.toSeconds(minutes);
        mCurrentProgressTextView.setText(String.format("%02d:%02d", minutes, seconds));

    }
    private void onPlay(boolean isPlaying) {

        if(!isPlaying){

            if(mediaPlayer == null){

               startPlaying();
            }else{

                resumePlaying();
            }

        }else{

            if(mediaPlayer != null)
                pausePlaying();

        }

    }


    private void startPlaying() {

        mediaPlayer = new MediaPlayer();
        mPlayButton.setImageResource(R.drawable.ic_pause);
        playAudio(true);

        if(mSeekBar.getProgress() > 0){

            animateSeekBar(300,0);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    updateCurrentTime(0);
                    updateSeekBar();
                }
            },300);

        }else{
            updateSeekBar();
        }


        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void updateSeekBar() {

        handler.postDelayed(mRunnable, SEEKBAR_SPEED);

    }

    private void animateSeekBar(int speed,int progress){

        ObjectAnimator animator = ObjectAnimator.ofInt(mSeekBar,"progress",progress);
        animator.setDuration(speed);
        animator.start();

    }


    private void resumePlaying() {

        mPlayButton.setImageResource(R.drawable.ic_pause);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.start();
        updateCurrentTime(mediaPlayer.getCurrentPosition());
        updateSeekBar();

    }

    @Override
    public void onPause() {
        super.onPause();

        if(mediaPlayer != null){

            pausePlaying();
            isPlaying = false;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if(mediaPlayer != null)
           stopPlaying();
    }

    private void pausePlaying() {

        mPlayButton.setImageResource(R.drawable.ic_media_play);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.pause();

    }

    private void playAudio(boolean autoStart) {

        try{

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                mediaPlayer.setDataSource(context,getRealPathFromUri(context,item.getName()));
            else
                mediaPlayer.setDataSource(item.getFilePAth());

            mediaPlayer.prepare();
            mSeekBar.setMax(mediaPlayer.getDuration());

            if(autoStart){

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                    }
                });

            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                   stopPlaying();

                    Log.d(LOG_TAG, "Stop Audio");
                }
            });


        }catch (IOException e){

            Log.e(LOG_TAG, "IOException ", e);
        }

    }

    private void stopPlaying() {

        mPlayButton.setImageResource(R.drawable.ic_replay);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        animateSeekBar(SEEKBAR_SPEED,mSeekBar.getMax());
        isPlaying = false;

        mCurrentProgressTextView.setText(mFileLengthTextView.getText());

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);

    }


}