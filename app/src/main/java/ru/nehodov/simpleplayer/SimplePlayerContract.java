package ru.nehodov.simpleplayer;

import android.content.Context;

public interface SimplePlayerContract {

    interface PlayerScreen {

        void setTrackInformation(int trackDuration, String trackName);

        Context getContext();
    }

}
