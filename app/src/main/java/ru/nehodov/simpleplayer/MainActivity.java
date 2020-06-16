package ru.nehodov.simpleplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageButton skipPrevious;
    ImageButton skipNext;
    SeekBar positionBar;
    TextView elapsingTimeLabel;
    TextView remainingTimeLabel;
    ScrollingTextView trackLabel;
    int trackDuration;

    int[] resourceIds;
    String[] trackLabels;

    int trackNumber = 0;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        skipPrevious = findViewById(R.id.skipPrevious);
        ImageButton play = findViewById(R.id.play);
        ImageButton pause = findViewById(R.id.pause);
        skipNext = findViewById(R.id.skipNext);
        positionBar = findViewById(R.id.positionBar);
        elapsingTimeLabel = findViewById(R.id.elapsingTimeLabel);
        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);
        trackLabel = (ScrollingTextView) findViewById(R.id.trackLabel);

        Field[] fields = R.raw.class.getFields();
        resourceIds = new int[fields.length];
        trackLabels = new String[fields.length];
        for(int count = 0; count < fields.length; count++){
            Log.i("Raw Asset: ", fields[count].getName());
            try {
                resourceIds[count] = fields[count].getInt(fields[count]);
//                Log.i("Raw Asset: ", String.valueOf(resourceIds[count]));
                trackLabels[count] = fields[count].getName();
                Log.i("Raw Asset: ", "track label:" + trackLabels[count]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (resourceIds.length > 0) {
            player = MediaPlayer.create(MainActivity.this, resourceIds[trackNumber]);
            trackDuration = player.getDuration();
            elapsingTimeLabel.setText(createTimeLabel(0));
            remainingTimeLabel.setText(createTimeLabel(trackDuration));
            trackLabel.setText(trackLabels[trackNumber]);
            positionBar.setMax(trackDuration);
        }
        positionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                    positionBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        play.setOnClickListener(onPlay -> player.start());
        pause.setOnClickListener(onPause -> player.pause());
        skipNext.setOnClickListener(this::onSkipNextClick);
        skipPrevious.setOnClickListener(this::onSkipPreviousClick);

        player.setOnCompletionListener(mp -> playTrack(++trackNumber));

        new Thread(() -> {
            while (player != null) {
                try {
                    Message message = new Message();
                    message.what = player.getCurrentPosition();
                    handler.sendMessage(message);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.release();
        player = null;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int currentPosition = msg.what;
            positionBar.setProgress(currentPosition);
            String elapsedTime = createTimeLabel(currentPosition);
            elapsingTimeLabel.setText(elapsedTime);
            String remainedTime = createTimeLabel(trackDuration - currentPosition);
            remainingTimeLabel.setText("- " + remainedTime);
        }
    };

    private String createTimeLabel(int time) {
        return String.format(Locale.US, "%d:%02d", time / 1000 / 60, time / 1000 % 60);
    }

    private void playTrack(int trackNumber) {
        player = MediaPlayer.create(MainActivity.this, resourceIds[trackNumber]);
        trackDuration = player.getDuration();
        positionBar.setMax(trackDuration);
        trackLabel.setText(trackLabels[trackNumber]);
        elapsingTimeLabel.setText(createTimeLabel(0));
        remainingTimeLabel.setText(createTimeLabel(trackDuration));
        player.start();
    }

    public void onSkipNextClick(View view) {
        trackNumber = (trackNumber + 1) >= resourceIds.length ? 0 : trackNumber + 1;
        player.release();
        playTrack(trackNumber);
    }

    public void onSkipPreviousClick(View view) {
        trackNumber = (trackNumber - 1) < 0 ? resourceIds.length - 1 : trackNumber - 1;
        player.release();
        playTrack(trackNumber);
    }
}