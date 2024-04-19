package com.example.cuoiky;

import java.io.Serializable;

public class MusicItem implements Serializable {
    private String trackName;
    private String displayImageUri;
    private String trackUri;  // Add a new field for trackUri
    private String duration;

    private String id;
    public MusicItem(String trackName, String displayImageUri, String trackUri, String duration,String id) {
        this.trackName = trackName;
        this.displayImageUri = displayImageUri;
        this.trackUri = trackUri;
        this.duration = duration;
        this.id = id;
    }

    public String getTrackName() {
        return trackName;
    }
    // Add a setter for trackUri
    public void setTrackURI(String trackUri) {
        this.trackUri = trackUri;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
    public String getDisplayImageUri() {
        return displayImageUri;
    }

    // Add a getter for trackUri
    public String getTrackURI() {
        return trackUri;
    }
    public String getDuration() {
        return duration;
    }
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return trackName;  // Adjust this based on how you want to display the item in the list
    }
}
