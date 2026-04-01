package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Song;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ListenLaterDAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListenLaterDAOTest {

    private ListenLaterDAO dao;
    private Connection conn;

    @BeforeAll
    public void setupDatabase() throws SQLException {
        DatabaseManager.getInstance().initializeDatabase();
        conn = DatabaseManager.getInstance().getConnection();
        dao = new ListenLaterDAO();
    }

    @BeforeEach
    public void clearListenLater() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM listen_later");
        }
    }

    @AfterAll
    public void cleanup() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    public void testAddSongToListenLater() {
        boolean result = dao.addSongToListenLater(1, 101);
        assertTrue(result, "Adding a song to listen later should succeed");
    }

    @Test
    public void testAddDuplicateSongToListenLater() {
        dao.addSongToListenLater(1, 101);

        boolean duplicate = dao.addSongToListenLater(1, 101);
        assertFalse(duplicate, "Adding the same song twice for the same user should return false");
    }

    @Test
    public void testRemoveSongFromListenLater() {
        dao.addSongToListenLater(1, 101);

        boolean removed = dao.removeSongFromListenLater(1, 101);
        assertTrue(removed, "Removing an existing entry should succeed");

        assertFalse(dao.isSongInListenLater(1, 101), "Song should no longer be in listen later after removal");
    }

    @Test
    public void testRemoveNonExistentSongFromListenLater() {
        boolean removed = dao.removeSongFromListenLater(1, 999);
        assertFalse(removed, "Removing a non-existent entry should return false");
    }

    @Test
    public void testGetListenLaterSongs() {
        dao.addSongToListenLater(2, 201);
        dao.addSongToListenLater(2, 202);

        List<Song> songs = dao.getListenLaterSongs(2);
        assertEquals(2, songs.size(), "Should return all songs in the user's listen later list");
    }

    @Test
    public void testGetListenLaterSongsOrderedByMostRecentFirst() {
        dao.addSongToListenLater(3, 301);
        dao.addSongToListenLater(3, 302);

        List<Song> songs = dao.getListenLaterSongs(3);
        assertFalse(songs.isEmpty(), "Should return songs for user");

        // Most recently added song should be first
        assertEquals(302, songs.get(0).getId(), "Most recently added song should appear first");
    }

    @Test
    public void testGetListenLaterSongsReturnsEmptyForUnknownUser() {
        List<Song> songs = dao.getListenLaterSongs(9999);
        assertNotNull(songs, "Result should not be null for a user with no entries");
        assertTrue(songs.isEmpty(), "Should return empty list for user with no listen later entries");
    }

    @Test
    public void testIsSongInListenLater() {
        dao.addSongToListenLater(1, 101);

        assertTrue(dao.isSongInListenLater(1, 101), "Should return true for a saved song");
        assertFalse(dao.isSongInListenLater(1, 999), "Should return false for a song not in the list");
    }

    @Test
    public void testCountListenLaterSongs() {
        dao.addSongToListenLater(4, 401);
        dao.addSongToListenLater(4, 402);
        dao.addSongToListenLater(4, 403);

        int count = dao.countListenLaterSongs(4);
        assertEquals(3, count, "Count should match the number of songs added");
    }

    @Test
    public void testCountListenLaterSongsReturnsZeroForUnknownUser() {
        int count = dao.countListenLaterSongs(9999);
        assertEquals(0, count, "Count should be 0 for a user with no listen later entries");
    }

    @Test
    public void testCountDecreasesAfterRemoval() {
        dao.addSongToListenLater(5, 501);
        dao.addSongToListenLater(5, 502);
        assertEquals(2, dao.countListenLaterSongs(5), "Count should be 2 before removal");

        dao.removeSongFromListenLater(5, 501);
        assertEquals(1, dao.countListenLaterSongs(5), "Count should decrease by 1 after removal");
    }
}