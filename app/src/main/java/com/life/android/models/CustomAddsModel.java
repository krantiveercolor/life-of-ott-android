package com.life.android.models;

import com.google.gson.annotations.SerializedName;

public class CustomAddsModel {
    private String id;
    private String video_url;
    private String redirect_url;
    private boolean played = false;
    @SerializedName("skip_at_seconds")
    private int skip_time;
    @SerializedName("play_add_at")
    private int playAddAtThisPosInMins;

    public String getId() {
        return id;
    }

    public String getVideo_url() {
        return video_url;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public int getSkip_time() {
        return skip_time;
    }

    public int getPlayAddAtThisPosInMins() {
        return playAddAtThisPosInMins;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }
}
