package com.musiccatalog.model;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String album;
    private int durationSeconds;
    private String genre;
    private int releaseYear;
    private String addedAt;

    public Song(int i, String blindingLights, String theWeeknd, String afterHours, int i1) {}

    public Song(int id, String title, String artist, String album,
                int durationSeconds, String genre, int releaseYear, String addedAt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationSeconds = durationSeconds;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.addedAt = addedAt;
    }

    public Song() {}

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public int getDurationSeconds() { return durationSeconds; }
    public String getGenre() { return genre; }
    public int getReleaseYear() { return releaseYear; }
    public String getAddedAt() { return addedAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public void setAddedAt(String addedAt) { this.addedAt = addedAt; }

    /**
     * Returns duration formatted as mm:ss
     */
    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return "Song{id=" + id + ", title='" + title + "', artist='" + artist + "', album='" + album + "'}";
    }
}
