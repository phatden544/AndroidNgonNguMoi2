package com.example.cuoiky;

import java.util.ArrayList;
import java.util.List;

public class MusicData {
    private static MusicData instance;
    private List<MusicItem> musicItems;

    private MusicData() {
        // Private constructor to prevent instantiation
        musicItems = new ArrayList<>();
    }

    public static synchronized MusicData getInstance() {
        if (instance == null) {
            instance = new MusicData();
        }
        return instance;
    }

    public List<MusicItem> getMusicItems() {
        return musicItems;
    }

    public void setMusicItems(List<MusicItem> musicItems) {
        this.musicItems = musicItems;
    }
}
