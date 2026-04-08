package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Song;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ListenLaterDAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ListenLaterDAOTest {

    private static final String FIRST_SONG = "Bohemian Rhapsody";
    private static final String SECOND_SONG = "Hotel California";
    private static final String THIRD_SONG = "Imagine";
    private static final String FOURTH_SONG = "Billie Jean";
    private static final String FIFTH_SONG = "Stairway to Heaven";

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
    public void testAddSongToListenLater() throws SQLException {
        boolean result = dao.addSongToListenLater(1, getSongId(FIRST_SONG));
        assertTrue(result, "Adding a song to listen later should succeed");
    }

    @Test
    public void testAddDuplicateSongToListenLater() throws SQLException {
        int songId = getSongId(FIRST_SONG);
        dao.addSongToListenLater(1, songId);

        boolean duplicate = dao.addSongToListenLater(1, songId);
        assertFalse(duplicate, "Adding the same song twice for the same user should return false");
    }

    @Test
    public void testRemoveSongFromListenLater() throws SQLException {
        int songId = getSongId(FIRST_SONG);
        dao.addSongToListenLater(1, songId);

        boolean removed = dao.removeSongFromListenLater(1, songId);
        assertTrue(removed, "Removing an existing entry should succeed");

        assertFalse(dao.isSongInListenLater(1, songId), "Song should no longer be in listen later after removal");
    }

    @Test
    public void testRemoveNonExistentSongFromListenLater() {
        boolean removed = dao.removeSongFromListenLater(1, 999);
        assertFalse(removed, "Removing a non-existent entry should return false");
    }

    @Test
    public void testGetListenLaterSongs() throws SQLException {
        dao.addSongToListenLater(2, getSongId(FIRST_SONG));
        dao.addSongToListenLater(2, getSongId(SECOND_SONG));

        List<Song> songs = dao.getListenLaterSongs(2);
        assertEquals(2, songs.size(), "Should return all songs in the user's listen later list");
    }

    @Test
    public void testGetListenLaterSongsOrderedByMostRecentFirst() throws SQLException {
        int olderSongId = getSongId(FIRST_SONG);
        int newerSongId = getSongId(SECOND_SONG);
        dao.addSongToListenLater(3, olderSongId);
        dao.addSongToListenLater(3, newerSongId);

        List<Song> songs = dao.getListenLaterSongs(3);
        assertFalse(songs.isEmpty(), "Should return songs for user");

        // Most recently added song should be first
        assertEquals(newerSongId, songs.get(0).getId(), "Most recently added song should appear first");
    }

    @Test
    public void testGetListenLaterSongsReturnsEmptyForUnknownUser() {
        List<Song> songs = dao.getListenLaterSongs(9999);
        assertNotNull(songs, "Result should not be null for a user with no entries");
        assertTrue(songs.isEmpty(), "Should return empty list for user with no listen later entries");
    }

    @Test
    public void testIsSongInListenLater() throws SQLException {
        int songId = getSongId(THIRD_SONG);
        dao.addSongToListenLater(1, songId);

        assertTrue(dao.isSongInListenLater(1, songId), "Should return true for a saved song");
        assertFalse(dao.isSongInListenLater(1, 999), "Should return false for a song not in the list");
    }

    @Test
    public void testCountListenLaterSongs() throws SQLException {
        dao.addSongToListenLater(4, getSongId(FIRST_SONG));
        dao.addSongToListenLater(4, getSongId(SECOND_SONG));
        dao.addSongToListenLater(4, getSongId(THIRD_SONG));

        int count = dao.countListenLaterSongs(4);
        assertEquals(3, count, "Count should match the number of songs added");
    }

    @Test
    public void testCountListenLaterSongsReturnsZeroForUnknownUser() {
        int count = dao.countListenLaterSongs(9999);
        assertEquals(0, count, "Count should be 0 for a user with no listen later entries");
    }

    @Test
    public void testCountDecreasesAfterRemoval() throws SQLException {
        int firstSongId = getSongId(FOURTH_SONG);
        int secondSongId = getSongId(FIFTH_SONG);
        dao.addSongToListenLater(5, firstSongId);
        dao.addSongToListenLater(5, secondSongId);
        assertEquals(2, dao.countListenLaterSongs(5), "Count should be 2 before removal");

        dao.removeSongFromListenLater(5, firstSongId);
        assertEquals(1, dao.countListenLaterSongs(5), "Count should decrease by 1 after removal");
    }

    private int getSongId(String title) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM songs WHERE title = ?")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Expected seeded song " + title);
                return rs.getInt("id");
            }
        }
    }
}
