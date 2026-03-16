package com.musiccatalog.model;

/**
 * Represents a song entry inside a playlist, including its position and the
 * timestamp it was added.
 */
public class PlaylistSong {
    private int playlistId;
    private Song song;
    private int position;
    private String addedAt;

    public PlaylistSong() {}

    public PlaylistSong(int playlistId, Song song, int position, String addedAt) {
        this.playlistId = playlistId;
        this.song       = song;
        this.position   = position;
        this.addedAt    = addedAt;
    }

    public int getPlaylistId()  { return playlistId; }
    public Song getSong()       { return song; }
    public int getPosition()    { return position; }
    public String getAddedAt()  { return addedAt; }

    public void setPlaylistId(int playlistId) { this.playlistId = playlistId; }
    public void setSong(Song song)            { this.song = song; }
    public void setPosition(int position)     { this.position = position; }
    public void setAddedAt(String addedAt)    { this.addedAt = addedAt; }

    @Override
    public String toString() {
        return "PlaylistSong{playlistId=" + playlistId
                + ", position=" + position
                + ", song=" + (song != null ? song.getTitle() : "null") + "}";
    }
}
