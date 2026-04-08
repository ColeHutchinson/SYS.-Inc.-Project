package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.PlaylistSong;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-03-TB: PlaylistDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaylistRemoveSongTest {

    private static final int DEMO_USER_ID = 2;
    private static final int SONG_A = 1; // will stay  — position 1
    private static final int SONG_B = 2; // will be removed
    private static final int SONG_C = 3; // will stay  — should compact to position 2

    private static PlaylistDAO playlistDAO;
    private static int playlistId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        playlistDAO = new PlaylistDAO();

        Playlist playlist = playlistDAO.createPlaylist(DEMO_USER_ID, "IT-03 Remove Test", null);
        assertNotNull(playlist, "Setup: createPlaylist() must succeed");
        playlistId = playlist.getId();

        assertTrue(playlistDAO.addSongToPlaylist(playlistId, SONG_A), "Setup: add song A at pos 1");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, SONG_B), "Setup: add song B at pos 2");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, SONG_C), "Setup: add song C at pos 3");
    }

    @Test
    @Order(1)
    @DisplayName("IT-03-TB Step 1: removeSongFromPlaylist() returns true for song B")
    void testRemoveSong() {
        boolean removed = playlistDAO.removeSongFromPlaylist(playlistId, SONG_B);
        assertTrue(removed, "removeSongFromPlaylist() should return true when the song exists");
    }

    @Test
    @Order(2)
    @DisplayName("IT-03-TB Step 2: getPlaylistSongEntries() shows 2 songs with compacted positions 1 and 2")
    void testCompactedPositions() {
        List<PlaylistSong> entries = playlistDAO.getPlaylistSongEntries(playlistId);

        assertEquals(2, entries.size(), "Only 2 songs should remain after removal");

        // Positions must be 1 and 2 — no gaps
        List<Integer> positions = entries.stream()
                .map(PlaylistSong::getPosition)
                .sorted()
                .toList();

        assertEquals(1, positions.get(0), "First remaining song should have position 1");
        assertEquals(2, positions.get(1), "Second remaining song should have position 2");

        // Song B must not be present
        boolean songBPresent = entries.stream()
                .anyMatch(e -> e.getSong().getId() == SONG_B);
        assertFalse(songBPresent, "Song B should not be in the playlist after removal");
    }

    @AfterAll
    static void tearDown() {
        if (playlistId > 0) {
            playlistDAO.deletePlaylist(playlistId);
        }
    }
}
