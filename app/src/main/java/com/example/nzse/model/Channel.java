package com.example.nzse.model;

public class Channel {
    public String id;
    public String provider;
    public String program;
    public int quality;

    public Channel(String id, String provider, String program) {
        this(id, provider, program, 0);
    }

    public Channel(String id, String provider, String program, int quality) {
        this.id = id;
        this.provider = provider;
        this.program = program;
        this.quality = quality;
    }
}
