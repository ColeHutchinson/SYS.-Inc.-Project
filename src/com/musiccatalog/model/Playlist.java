package com.musiccatalog.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user-owned playlist.
 */
public class Playlist {
    private int id;
    private int userId;
    private String name;
    private String description;
    private String createdAt;
    private List<Song> songs;

    public Playlist() {
        this.songs = new ArrayList<>();
    }

    public Playlist(int id, int userId, String name, String description, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.songs = new ArrayList<>();
    }

    // Getters
    public int getId()            { return id; }
    public int getUserId()        { return userId; }
    public String getName()       { return name; }
    public String getDescription(){ return description; }
    public String getCreatedAt()  { return createdAt; }
    public List<Song> getSongs()  { return songs; }

    // Setters
    public void setId(int id)                     { this.id = id; }
    public void setUserId(int userId)             { this.userId = userId; }
    public void setName(String name)              { this.name = name; }
    public void setDescription(String description){ this.description = description; }
    public void setCreatedAt(String createdAt)    { this.createdAt = createdAt; }
    public void setSongs(List<Song> songs)        { this.songs = songs; }

    /** Total duration of all songs in the playlist, in seconds. */
    public int getTotalDurationSeconds() {
        return songs.stream().mapToInt(Song::getDurationSeconds).sum();
    }

    /** Total duration formatted as h:mm:ss or mm:ss. */
    public String getFormattedTotalDuration() {
        int total = getTotalDurationSeconds();
        int hours   = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return "Playlist{id=" + id + ", userId=" + userId + ", name='" + name + "', songs=" + songs.size() + "}";
    }
}
