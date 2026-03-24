package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.SongSuggestion;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SongSuggestionDAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SongSuggestionDAOTest {

    private SongSuggestionDAO dao;
    private Connection conn;

    @BeforeAll
    public void setupDatabase() throws SQLException {
        // Initialize database
        DatabaseManager.getInstance().initializeDatabase();
        conn = DatabaseManager.getInstance().getConnection();
        dao = new SongSuggestionDAO();
    }

    @BeforeEach
    public void clearSuggestions() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM song_suggestions");
        }
    }

    @AfterAll
    public void cleanup() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    public void testAddSuggestion() {
        SongSuggestion suggestion = new SongSuggestion();
        suggestion.setTitle("Test Song");
        suggestion.setArtist("Test Artist");
        suggestion.setAlbum("Test Album");
        suggestion.setDurationSeconds(180);
        suggestion.setGenre("Test Genre");
        suggestion.setReleaseYear(2023);
        suggestion.setSuggestedBy(1);
        suggestion.setStatus("PENDING");

        boolean result = dao.addSuggestion(suggestion);
        assertTrue(result, "Adding suggestion should succeed");

        // Verify it was added
        List<SongSuggestion> suggestions = dao.findAll(null);
        assertFalse(suggestions.isEmpty(), "Should have at least one suggestion");

        SongSuggestion added = suggestions.get(suggestions.size() - 1);
        assertEquals("Test Song", added.getTitle());
        assertEquals("Test Artist", added.getArtist());
        assertEquals("PENDING", added.getStatus());
        assertEquals(1, added.getSuggestedBy());
    }

    @Test
    public void testFindAllWithStatus() {
        // Add a few suggestions
        SongSuggestion pending = new SongSuggestion();
        pending.setTitle("Pending Song");
        pending.setArtist("Artist");
        pending.setDurationSeconds(200);
        pending.setSuggestedBy(2);
        pending.setStatus("PENDING");
        dao.addSuggestion(pending);

        List<SongSuggestion> allPending = dao.findAll(SongSuggestionDAO.Status.PENDING);
        assertTrue(allPending.stream().anyMatch(s -> "Pending Song".equals(s.getTitle())),
                   "Should find pending suggestion");
    }

    @Test
    public void testFindByUser() {
        int userId = 3;
        SongSuggestion userSuggestion = new SongSuggestion();
        userSuggestion.setTitle("User Song");
        userSuggestion.setArtist("User Artist");
        userSuggestion.setDurationSeconds(250);
        userSuggestion.setSuggestedBy(userId);
        userSuggestion.setStatus("PENDING");
        dao.addSuggestion(userSuggestion);

        List<SongSuggestion> userSuggestions = dao.findByUser(userId);
        assertFalse(userSuggestions.isEmpty(), "Should find suggestions for user");
        assertEquals("User Song", userSuggestions.get(0).getTitle());
    }

    @Test
    public void testDeleteSuggestion() {
        // Add a suggestion
        SongSuggestion suggestion = new SongSuggestion();
        suggestion.setTitle("Delete Test Song");
        suggestion.setArtist("Artist");
        suggestion.setDurationSeconds(210);
        suggestion.setSuggestedBy(1);
        suggestion.setStatus("PENDING");
        dao.addSuggestion(suggestion);

        // Find it
        List<SongSuggestion> suggestions = dao.findAll(null);
        SongSuggestion added = suggestions.stream()
                .filter(s -> "Delete Test Song".equals(s.getTitle()))
                .findFirst().orElse(null);
        assertNotNull(added, "Should find the added suggestion");

        // Delete it
        boolean deleted = dao.deleteSuggestion(added.getId());
        assertTrue(deleted, "Deletion should succeed");

        // Verify it's gone
        SongSuggestion deletedSuggestion = dao.findById(added.getId());
        assertNull(deletedSuggestion, "Suggestion should be deleted");
    }

    @Test
    public void testFindById() {
        // Add a suggestion
        SongSuggestion suggestion = new SongSuggestion();
        suggestion.setTitle("Find By ID Song");
        suggestion.setArtist("Artist");
        suggestion.setDurationSeconds(230);
        suggestion.setSuggestedBy(1);
        suggestion.setStatus("PENDING");
        dao.addSuggestion(suggestion);

        // Find all to get ID
        List<SongSuggestion> suggestions = dao.findAll(null);
        SongSuggestion added = suggestions.stream()
                .filter(s -> "Find By ID Song".equals(s.getTitle()))
                .findFirst().orElse(null);
        assertNotNull(added, "Should find the added suggestion");

        // Find by ID
        SongSuggestion found = dao.findById(added.getId());
        assertNotNull(found, "Should find suggestion by ID");
        assertEquals("Find By ID Song", found.getTitle());
    }
}
