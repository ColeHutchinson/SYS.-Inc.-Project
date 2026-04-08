package com.musiccatalog.service;

import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.dao.SongSuggestionDAO;
import com.musiccatalog.model.Song;
import com.musiccatalog.model.SongSuggestion;
import com.musiccatalog.model.User;

/**
 * Handles song submissions based on user role.
 */
public class SongSubmissionService {

    public enum Result {
        ADDED,
        SUGGESTED,
        FAILED
    }

    private final SongDAO songDAO;
    private final SongSuggestionDAO suggestionDAO;

    public SongSubmissionService(SongDAO songDAO, SongSuggestionDAO suggestionDAO) {
        this.songDAO = songDAO;
        this.suggestionDAO = suggestionDAO;
    }

    public Result submit(User user, Song song) {
        if (user == null || song == null) {
            return Result.FAILED;
        }
        if (user.isAdmin()) {
            return songDAO.addSong(song) ? Result.ADDED : Result.FAILED;
        }
        SongSuggestion suggestion = new SongSuggestion();
        suggestion.setTitle(song.getTitle());
        suggestion.setArtist(song.getArtist());
        suggestion.setAlbum(song.getAlbum());
        suggestion.setDurationSeconds(song.getDurationSeconds());
        suggestion.setGenre(song.getGenre());
        int year = song.getReleaseYear();
        suggestion.setReleaseYear(year > 0 ? year : null);
        suggestion.setSuggestedBy(user.getId());
        suggestion.setStatus(SongSuggestionDAO.Status.PENDING.name());
        return suggestionDAO.addSuggestion(suggestion) ? Result.SUGGESTED : Result.FAILED;
    }
}
