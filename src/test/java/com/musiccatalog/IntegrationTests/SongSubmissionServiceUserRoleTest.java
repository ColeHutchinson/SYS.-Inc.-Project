package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.dao.SongSuggestionDAO;
import com.musiccatalog.dao.UserDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Song;
import com.musiccatalog.model.SongSuggestion;
import com.musiccatalog.model.User;
import com.musiccatalog.service.SongSubmissionService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-06-TB: SongSubmissionService + SongSuggestionDAO + SongDAO
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SongSubmissionServiceUserRoleTest {

    private static SongSubmissionService submissionService;
    private static SongDAO songDAO;
    private static SongSuggestionDAO suggestionDAO;

    private static int songCountBefore;
    private static User demoUser;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        songDAO = new SongDAO();
        suggestionDAO = new SongSuggestionDAO();
        submissionService = new SongSubmissionService(songDAO, suggestionDAO);

        demoUser = new UserDAO().findByUsername("demo");
        assertNotNull(demoUser, "Setup: expected seeded demo user");
        songCountBefore = songDAO.countAll();
    }

    @Test
    @Order(1)
    @DisplayName("IT-06-TB Step 1: submit() returns SUGGESTED for a USER-role account")
    void testSubmitReturnsSuggested() {
        Song song = new Song(0, "User Submitted Song", "Some Artist", "Some Album", 210, "Indie", 2024, null);
        SongSubmissionService.Result result = submissionService.submit(demoUser, song);

        assertEquals(SongSubmissionService.Result.SUGGESTED, result,
                "submit() should return SUGGESTED when the user does not have ADMIN role");
    }

    @Test
    @Order(2)
    @DisplayName("IT-06-TB Step 2: a suggestion record is created in song_suggestions for the demo user")
    void testSuggestionRecordCreated() {
        List<SongSuggestion> suggestions = suggestionDAO.findByUser(demoUser.getId());
        assertNotNull(suggestions);

        boolean found = suggestions.stream()
                .anyMatch(s -> "User Submitted Song".equals(s.getTitle()) &&
                               s.getSuggestedBy() == demoUser.getId());

        assertTrue(found, "A suggestion record should exist in song_suggestions for the submitted song");
    }

    @Test
    @Order(3)
    @DisplayName("IT-06-TB Step 3: the songs table is unchanged — no direct insert occurred")
    void testSongsTableUnchanged() {
        int songCountAfter = songDAO.countAll();
        assertEquals(songCountBefore, songCountAfter,
                "The songs table should not grow when a USER submits a song");
    }

    @AfterAll
    static void tearDown() {
        suggestionDAO.findByUser(demoUser.getId()).stream()
                .filter(s -> "User Submitted Song".equals(s.getTitle()))
                .forEach(s -> suggestionDAO.deleteSuggestion(s.getId()));
    }
}
