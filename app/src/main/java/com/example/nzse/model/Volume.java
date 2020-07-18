package com.example.nzse.model;

public class Volume {
    public int volume;
    public boolean muted;

    public Volume(int volume) {
        this(volume, false);
    }

    public Volume(int volume, boolean muted) {
        this.volume = volume;
        this.muted = muted;
    }
}
