package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.SongSuggestionDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.SongSuggestion;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-04-TB: SongSuggestionDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SongSuggestionAddAndFindByUserTest {

    private static final int DEMO_USER_ID = 2;

    private static SongSuggestionDAO suggestionDAO;
    private static SongSuggestion testSuggestion;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        suggestionDAO = new SongSuggestionDAO();

        testSuggestion = new SongSuggestion();
        testSuggestion.setTitle("Integration Test Song");
        testSuggestion.setArtist("Test Artist");
        testSuggestion.setAlbum("Test Album");
        testSuggestion.setDurationSeconds(200);
        testSuggestion.setGenre("Rock");
        testSuggestion.setReleaseYear(2024);
        testSuggestion.setSuggestedBy(DEMO_USER_ID);
        testSuggestion.setStatus(SongSuggestionDAO.Status.PENDING.name());
    }

    @Test
    @Order(1)
    @DisplayName("IT-04-TB Step 1: addSuggestion() returns true and persists to DB")
    void testAddSuggestion() {
        boolean added = suggestionDAO.addSuggestion(testSuggestion);
        assertTrue(added, "addSuggestion() should return true on success");
    }

    @Test
    @Order(2)
    @DisplayName("IT-04-TB Step 2: findByUser() returns the suggestion associated with the demo user")
    void testFindByUser() {
        List<SongSuggestion> results = suggestionDAO.findByUser(DEMO_USER_ID);

        assertNotNull(results);
        assertFalse(results.isEmpty(), "findByUser() should return at least one suggestion");

        boolean found = results.stream().anyMatch(s ->
                "Integration Test Song".equals(s.getTitle()) &&
                "Test Artist".equals(s.getArtist()) &&
                s.getSuggestedBy() == DEMO_USER_ID
        );

        assertTrue(found, "The suggestion added in step 1 should be returned for the demo user");
    }

    @AfterAll
    static void tearDown() {
        // Clean up: delete any test suggestions left by this run
        List<SongSuggestion> all = suggestionDAO.findByUser(DEMO_USER_ID);
        all.stream()
           .filter(s -> "Integration Test Song".equals(s.getTitle()))
           .forEach(s -> suggestionDAO.deleteSuggestion(s.getId()));
    }
}
