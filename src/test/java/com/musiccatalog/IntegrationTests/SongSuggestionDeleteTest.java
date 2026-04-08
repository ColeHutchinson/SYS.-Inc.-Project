package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.SongSuggestionDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.SongSuggestion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-05-TB: SongSuggestionDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SongSuggestionDeleteTest {

    private static final int DEMO_USER_ID = 2;

    private static SongSuggestionDAO suggestionDAO;
    private static int suggestionId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        suggestionDAO = new SongSuggestionDAO();

        // Insert a suggestion to delete in the test
        SongSuggestion suggestion = new SongSuggestion();
        suggestion.setTitle("Song To Delete");
        suggestion.setArtist("Temp Artist");
        suggestion.setAlbum("Temp Album");
        suggestion.setDurationSeconds(180);
        suggestion.setGenre("Pop");
        suggestion.setReleaseYear(2023);
        suggestion.setSuggestedBy(DEMO_USER_ID);
        suggestion.setStatus(SongSuggestionDAO.Status.PENDING.name());

        boolean added = suggestionDAO.addSuggestion(suggestion);
        assertTrue(added, "Setup: addSuggestion() must succeed to seed the test record");

        // Retrieve the generated id
        suggestionId = suggestionDAO.findByUser(DEMO_USER_ID).stream()
                .filter(s -> "Song To Delete".equals(s.getTitle()))
                .mapToInt(SongSuggestion::getId)
                .findFirst()
                .orElse(-1);

        assertNotEquals(-1, suggestionId, "Setup: could not locate the newly inserted suggestion");
    }

    @Test
    @Order(1)
    @DisplayName("IT-05-TB Step 1: deleteSuggestion() returns true for an existing suggestion")
    void testDeleteSuggestion() {
        boolean deleted = suggestionDAO.deleteSuggestion(suggestionId);
        assertTrue(deleted, "deleteSuggestion() should return true when the record exists");
    }

    @Test
    @Order(2)
    @DisplayName("IT-05-TB Step 2: findById() returns null after deletion — record no longer retrievable")
    void testFindByIdAfterDelete() {
        SongSuggestion result = suggestionDAO.findById(suggestionId);
        assertNull(result, "findById() should return null for a deleted suggestion");
    }
}
