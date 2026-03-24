package com.musiccatalog.model;

public class SongSuggestion {
    private int id;
    private String title;
    private String artist;
    private String album;
    private int durationSeconds;
    private String genre;
    private Integer releaseYear;
    private int suggestedBy;
    private String status;
    private String suggestedAt;
    private String reviewedAt;
    private Integer reviewedBy;

    public SongSuggestion() {}

    public SongSuggestion(int id, String title, String artist, String album, int durationSeconds,
                         String genre, Integer releaseYear, int suggestedBy, String status,
                         String suggestedAt, String reviewedAt, Integer reviewedBy) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationSeconds = durationSeconds;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.suggestedBy = suggestedBy;
        this.status = status;
        this.suggestedAt = suggestedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedBy = reviewedBy;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public int getSuggestedBy() { return suggestedBy; }
    public void setSuggestedBy(int suggestedBy) { this.suggestedBy = suggestedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSuggestedAt() { return suggestedAt; }
    public void setSuggestedAt(String suggestedAt) { this.suggestedAt = suggestedAt; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(status);
    }

    @Override
    public String toString() {
        return "SongSuggestion{id=" + id + ", title='" + title + "', artist='" + artist +
               "', status='" + status + "', suggestedBy=" + suggestedBy + "}";
    }
}