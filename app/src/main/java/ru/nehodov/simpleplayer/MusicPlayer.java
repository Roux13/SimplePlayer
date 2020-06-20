package ru.nehodov.simpleplayer;

import android.media.MediaPlayer;

import java.lang.reflect.Field;

public class MusicPlayer {

    private MediaPlayer player;
    private int[] resourceIds;
    private String[] trackNames;
    private int trackDuration;
    private int currentTrackNumber = -1;

    private SimplePlayerContract.PlayerScreen screen;

    public MusicPlayer(SimplePlayerContract.PlayerScreen screen) {
        this.screen = screen;
        prepareTrackList();
    }

    public void prepareTrackList() {
        Field[] fields = R.raw.class.getFields();
        resourceIds = new int[fields.length];
        trackNames = new String[fields.length];
        for (int count = 0; count < fields.length; count++) {
            try {
                resourceIds[count] = fields[count].getInt(fields[count]);
                trackNames[count] = fields[count].getName();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (resourceIds.length > 0) {
            preparePlayer(++currentTrackNumber);
        }
    }

    public void skipPreviousTrack() {
        currentTrackNumber =
                (currentTrackNumber - 1) < 0 ? resourceIds.length - 1 : currentTrackNumber - 1;
        player.release();
        preparePlayer(currentTrackNumber);
        playTrack();
    }

    public void skipNextTrack() {
        currentTrackNumber =
                (currentTrackNumber + 1) >= resourceIds.length ? 0 : currentTrackNumber + 1;
        player.release();
        preparePlayer(currentTrackNumber);
        playTrack();
    }

    private void preparePlayer(int trackNumber) {
        player = MediaPlayer.create(screen.getContext(), resourceIds[trackNumber]);
        trackDuration = player.getDuration();
        screen.setTrackInformation(trackDuration, trackNames[trackNumber]);
        player.setOnCompletionListener(mp -> skipNextTrack());
    }

    private void playTrack() {
        if (resourceIds.length > 0) {
            player.start();
        }
    }

    public int getTrackDuration() {
        return trackDuration;
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void attachScreen(SimplePlayerContract.PlayerScreen screen) {
        this.screen = screen;
    }

    public void detachScreen() {
        this.screen = null;
    }

    public void release() {
        player.release();
    }

    public void seekTo(int progress) {
        player.seekTo(progress);
    }

    public void start() {
        playTrack();
    }

    public void pause() {
        player.pause();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }
}
