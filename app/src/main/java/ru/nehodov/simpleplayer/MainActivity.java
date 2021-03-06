package ru.nehodov.simpleplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements SimplePlayerContract.PlayerScreen {

    private static final String TAG = MainActivity.class.getName();

    private SeekBar positionBar;
    private TextView elapsingTimeLabel;
    private TextView remainingTimeLabel;
    private ScrollingTextView trackLabel;

    private MusicPlayer player;

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton skipPrevious = findViewById(R.id.skipPrevious);
        ImageButton play = findViewById(R.id.play);
        ImageButton pause = findViewById(R.id.pause);
        ImageButton skipNext = findViewById(R.id.skipNext);
        positionBar = findViewById(R.id.positionBar);
        elapsingTimeLabel = findViewById(R.id.elapsingTimeLabel);
        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);
        trackLabel = findViewById(R.id.trackLabel);

        if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Intent intent = getIntent();
            Log.d(TAG, "There is an intent with type: " + intent.getType()
                    + " with action: " + intent.getAction() + " data: " + intent.getData());
            player = new MusicPlayer(this, intent.getData());
        } else {
            Log.d(TAG, "There are no intents");
            player = new MusicPlayer(this);
        }
        player.attachScreen(this);

        if (this.disposable == null) {
            this.disposable = Observable.interval(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v -> {
                        if (player != null) {
                            changeTrackInformation(
                                    player.getCurrentPosition(), player.getTrackDuration());
                        }
                    });
        }

        positionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (player != null) {
                        player.seekTo(progress);
                        positionBar.setProgress(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        play.setOnClickListener(this::start);
        pause.setOnClickListener(onPause -> player.pause());
        skipNext.setOnClickListener(this::onSkipNextClick);
        skipPrevious.setOnClickListener(this::onSkipPreviousClick);
    }

    public void start(View view) {
        if (player == null) {
            player = new MusicPlayer(this);
            player.attachScreen(this);
        }
        player.start();
    }

    @Override
    public void setTrackInformation(int trackDuration, String trackName) {
        elapsingTimeLabel.setText(createTimeLabel(0));
        remainingTimeLabel.setText(createTimeLabel(trackDuration));
        trackLabel.setText(trackName);
        positionBar.setMax(trackDuration);
    }

    public void changeTrackInformation(int currentPosition, int duration) {
        positionBar.setProgress(currentPosition);
        elapsingTimeLabel.setText(createTimeLabel(currentPosition));
        String remainedTime = createTimeLabel(duration - currentPosition);
        remainingTimeLabel.setText(String.format(getString(R.string.remainedTime), remainedTime));
    }

    @Override
    public Context getContext() {
        return this;
    }

    public void onSkipNextClick(View view) {
        player.skipNextTrack();
    }

    public void onSkipPreviousClick(View view) {
        player.skipPreviousTrack();
    }

    private String createTimeLabel(int time) {
        return String.format(Locale.US, "%d:%02d", time / 1000 / 60, time / 1000 % 60);
    }

    @Override
    protected void onStop() {
        player.release();
        player.detachScreen();
        player = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.disposable != null) {
            this.disposable.dispose();
        }
    }

}