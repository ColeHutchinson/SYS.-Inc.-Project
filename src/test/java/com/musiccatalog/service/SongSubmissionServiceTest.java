package com.musiccatalog.service;

import com.musiccatalog.service.SongSubmissionService;

import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.dao.SongSuggestionDAO;
import com.musiccatalog.model.Song;
import com.musiccatalog.model.SongSuggestion;
import com.musiccatalog.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SongSubmissionServiceTest {

    @Test
    public void regularUserSubmitsSuggestion() {
        CapturingSuggestionDAO suggestionDAO = new CapturingSuggestionDAO();
        SongSubmissionService service = new SongSubmissionService(new RejectingSongDAO(), suggestionDAO);

        User user = new User();
        user.setId(42);
        user.setRole("USER");

        Song song = new Song(0, "Test Title", "Test Artist", "Test Album", 210, "Rock", 2024, null);

        SongSubmissionService.Result result = service.submit(user, song);

        assertEquals(SongSubmissionService.Result.SUGGESTED, result);
        assertNotNull(suggestionDAO.captured, "Suggestion should be captured");
        assertEquals("Test Title", suggestionDAO.captured.getTitle());
        assertEquals("Test Artist", suggestionDAO.captured.getArtist());
        assertEquals("Test Album", suggestionDAO.captured.getAlbum());
        assertEquals(210, suggestionDAO.captured.getDurationSeconds());
        assertEquals("Rock", suggestionDAO.captured.getGenre());
        assertEquals(2024, suggestionDAO.captured.getReleaseYear());
        assertEquals(42, suggestionDAO.captured.getSuggestedBy());
        assertEquals("PENDING", suggestionDAO.captured.getStatus());
    }

    private static class RejectingSongDAO extends SongDAO {
        @Override
        public boolean addSong(Song song) {
            throw new AssertionError("Regular user should not add songs directly");
        }
    }

    private static class CapturingSuggestionDAO extends SongSuggestionDAO {
        private SongSuggestion captured;

        @Override
        public boolean addSuggestion(SongSuggestion suggestion) {
            this.captured = suggestion;
            return true;
        }
    }
}
