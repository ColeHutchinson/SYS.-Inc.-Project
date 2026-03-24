package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.PlaylistSong;
import com.musiccatalog.model.Song;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistDAOTest {

    private static final String TEST_USER = "demo";
    private static final String FIRST_SONG = "Bohemian Rhapsody";
    private static final String SECOND_SONG = "Hotel California";
    private static final String THIRD_SONG = "Imagine";

    private static Connection conn;

    private final PlaylistDAO dao = new PlaylistDAO();

    @BeforeAll
    static void setupDatabase() throws SQLException {
        DatabaseManager.getInstance().initializeDatabase();
        conn = DatabaseManager.getInstance().getConnection();
    }

    @BeforeEach
    void clearPlaylists() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM playlist_songs");
            stmt.executeUpdate("DELETE FROM playlists");
        }
    }

    @Test
    void createPlaylistReturnsPersistedPlaylist() throws SQLException {
        int userId = getUserId(TEST_USER);

        Playlist created = dao.createPlaylist(userId, "Road Trip", "High-energy songs");
        assertNotNull(created, "Playlist should be created");
        assertTrue(created.getId() > 0, "Playlist should have a generated id");
        assertEquals("Road Trip", created.getName());
        assertEquals("High-energy songs", created.getDescription());
    }

    @Test
    void getPlaylistsByUserReturnsCreatedPlaylist() throws SQLException {
        int userId = getUserId(TEST_USER);

        Playlist created = dao.createPlaylist(userId, "Road Trip", "High-energy songs");
        assertNotNull(created, "Playlist should be created");

        List<Playlist> userPlaylists = dao.getPlaylistsByUser(userId);
        assertEquals(1, userPlaylists.size(), "User should have one playlist");
        assertEquals(created.getId(), userPlaylists.get(0).getId());
    }

    @Test
    void updatePlaylistPersistsNewValues() throws SQLException {
        int userId = getUserId(TEST_USER);

        Playlist created = dao.createPlaylist(userId, "Road Trip", "High-energy songs");
        assertNotNull(created, "Playlist should be created");

        boolean updated = dao.updatePlaylist(created.getId(), "Late Night Drive", "Slower mix");
        assertTrue(updated, "Playlist update should succeed");

        Playlist reloaded = dao.getPlaylistById(created.getId());
        assertNotNull(reloaded, "Updated playlist should still exist");
        assertEquals("Late Night Drive", reloaded.getName());
        assertEquals("Slower mix", reloaded.getDescription());
    }

    @Test
    void deletePlaylistRemovesItFromStorage() throws SQLException {
        int userId = getUserId(TEST_USER);

        Playlist created = dao.createPlaylist(userId, "Road Trip", "High-energy songs");
        assertNotNull(created, "Playlist should be created");

        boolean deleted = dao.deletePlaylist(created.getId());
        assertTrue(deleted, "Playlist deletion should succeed");
        assertNull(dao.getPlaylistById(created.getId()), "Deleted playlist should not be found");
        assertTrue(dao.getPlaylistsByUser(userId).isEmpty(), "User should have no playlists left");
    }

    @Test
    void addSongToPlaylistStoresSongsInInsertionOrder() throws SQLException {
        int userId = getUserId(TEST_USER);
        int firstSongId = getSongId(FIRST_SONG);
        int secondSongId = getSongId(SECOND_SONG);
        int thirdSongId = getSongId(THIRD_SONG);

        Playlist playlist = dao.createPlaylist(userId, "Favorites", "Testing song order");
        assertNotNull(playlist, "Playlist should be created before adding songs");

        assertTrue(dao.addSongToPlaylist(playlist.getId(), firstSongId), "Should add first song");
        assertTrue(dao.addSongToPlaylist(playlist.getId(), secondSongId), "Should add second song");
        assertTrue(dao.addSongToPlaylist(playlist.getId(), thirdSongId), "Should add third song");
        assertFalse(dao.addSongToPlaylist(playlist.getId(), firstSongId), "Duplicate song should be ignored");

        assertTrue(dao.containsSong(playlist.getId(), secondSongId), "Playlist should contain the second song");

        List<Song> initialSongs = dao.getSongsForPlaylist(playlist.getId());
        assertEquals(List.of(FIRST_SONG, SECOND_SONG, THIRD_SONG),
            initialSongs.stream().map(Song::getTitle).toList(),
            "Songs should be returned in insertion order");
    }

    @Test
    void addSongToPlaylistIgnoresDuplicateSong() throws SQLException {
        int userId = getUserId(TEST_USER);
        int firstSongId = getSongId(FIRST_SONG);

        Playlist playlist = dao.createPlaylist(userId, "Favorites", "Testing duplicates");
        assertNotNull(playlist, "Playlist should be created before adding songs");

        assertTrue(dao.addSongToPlaylist(playlist.getId(), firstSongId), "Should add first song");
        assertFalse(dao.addSongToPlaylist(playlist.getId(), firstSongId), "Duplicate song should be ignored");
    }

    @Test
    void containsSongReturnsTrueForExistingPlaylistEntry() throws SQLException {
        int userId = getUserId(TEST_USER);
        int secondSongId = getSongId(SECOND_SONG);

        Playlist playlist = dao.createPlaylist(userId, "Favorites", "Testing membership");
        assertNotNull(playlist, "Playlist should be created before membership checks");

        assertTrue(dao.addSongToPlaylist(playlist.getId(), secondSongId), "Should add second song");
        assertTrue(dao.containsSong(playlist.getId(), secondSongId), "Playlist should contain the second song");
    }

    @Test
    void moveSongReordersPlaylistSongs() throws SQLException {
        int userId = getUserId(TEST_USER);
        int firstSongId = getSongId(FIRST_SONG);
        int secondSongId = getSongId(SECOND_SONG);
        int thirdSongId = getSongId(THIRD_SONG);

        Playlist playlist = dao.createPlaylist(userId, "Favorites", "Testing song order");
        assertNotNull(playlist, "Playlist should be created before moving songs");

        assertTrue(dao.addSongToPlaylist(playlist.getId(), firstSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), secondSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), thirdSongId));

        assertTrue(dao.moveSong(playlist.getId(), thirdSongId, 1), "Should move third song to the top");

        Playlist withSongs = dao.getPlaylistWithSongs(playlist.getId());
        assertNotNull(withSongs, "Playlist with songs should load");
        assertEquals(List.of(THIRD_SONG, FIRST_SONG, SECOND_SONG),
            withSongs.getSongs().stream().map(Song::getTitle).toList(),
            "Playlist should reflect the new order");
    }

    @Test
    void getPlaylistSongEntriesReturnsOrderedMetadata() throws SQLException {
        int userId = getUserId(TEST_USER);
        int firstSongId = getSongId(FIRST_SONG);
        int secondSongId = getSongId(SECOND_SONG);
        int thirdSongId = getSongId(THIRD_SONG);

        Playlist playlist = dao.createPlaylist(userId, "Favorites", "Testing entry metadata");
        assertNotNull(playlist, "Playlist should be created before reading entries");

        assertTrue(dao.addSongToPlaylist(playlist.getId(), firstSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), secondSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), thirdSongId));
        assertTrue(dao.moveSong(playlist.getId(), thirdSongId, 1));

        List<PlaylistSong> entries = dao.getPlaylistSongEntries(playlist.getId());
        assertEquals(3, entries.size(), "Playlist should have three entry rows");
        assertEquals(1, entries.get(0).getPosition());
        assertEquals(THIRD_SONG, entries.get(0).getSong().getTitle());
        assertEquals(2, entries.get(1).getPosition());
        assertEquals(FIRST_SONG, entries.get(1).getSong().getTitle());
        assertEquals(3, entries.get(2).getPosition());
        assertEquals(SECOND_SONG, entries.get(2).getSong().getTitle());
        assertNotNull(entries.get(0).getAddedAt(), "Playlist entry timestamp should be populated");
    }

    @Test
    void removeSongFromPlaylistDeletesTheSong() throws SQLException {
        int userId = getUserId(TEST_USER);
        int firstSongId = getSongId(FIRST_SONG);
        int secondSongId = getSongId(SECOND_SONG);
        int thirdSongId = getSongId(THIRD_SONG);

        Playlist playlist = dao.createPlaylist(userId, "Cleanup", "Testing removals");
        assertNotNull(playlist, "Playlist should be created before removal checks");

        assertTrue(dao.addSongToPlaylist(playlist.getId(), firstSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), secondSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), thirdSongId));

        assertTrue(dao.removeSongFromPlaylist(playlist.getId(), secondSongId), "Middle song should be removed");
        assertFalse(dao.containsSong(playlist.getId(), secondSongId), "Removed song should no longer exist in playlist");
    }

    @Test
    void removeSongFromPlaylistCompactsPositions() throws SQLException {
        int userId = getUserId(TEST_USER);
        int firstSongId = getSongId(FIRST_SONG);
        int secondSongId = getSongId(SECOND_SONG);
        int thirdSongId = getSongId(THIRD_SONG);

        Playlist playlist = dao.createPlaylist(userId, "Cleanup", "Testing removals");
        assertNotNull(playlist, "Playlist should be created before removal checks");

        assertTrue(dao.addSongToPlaylist(playlist.getId(), firstSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), secondSongId));
        assertTrue(dao.addSongToPlaylist(playlist.getId(), thirdSongId));
        assertTrue(dao.removeSongFromPlaylist(playlist.getId(), secondSongId));

        List<PlaylistSong> entries = dao.getPlaylistSongEntries(playlist.getId());
        assertEquals(2, entries.size(), "Playlist should have two songs after removal");
        assertEquals(FIRST_SONG, entries.get(0).getSong().getTitle());
        assertEquals(1, entries.get(0).getPosition(), "First remaining song should be re-numbered to position 1");
        assertEquals(THIRD_SONG, entries.get(1).getSong().getTitle());
        assertEquals(2, entries.get(1).getPosition(), "Second remaining song should be re-numbered to position 2");
    }

    private int getUserId(String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Expected seeded user " + username);
                return rs.getInt("id");
            }
        }
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
