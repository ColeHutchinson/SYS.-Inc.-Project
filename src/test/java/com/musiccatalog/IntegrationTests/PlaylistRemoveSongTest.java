package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.dao.UserDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.PlaylistSong;
import com.musiccatalog.model.User;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-03-TB: PlaylistDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaylistRemoveSongTest {

    private static final String SONG_A_TITLE = "Bohemian Rhapsody";
    private static final String SONG_B_TITLE = "Hotel California";
    private static final String SONG_C_TITLE = "Stairway to Heaven";

    private static PlaylistDAO playlistDAO;
    private static SongDAO songDAO;
    private static int demoUserId;
    private static int songAId;
    private static int songBId;
    private static int songCId;
    private static int playlistId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        playlistDAO = new PlaylistDAO();
        songDAO = new SongDAO();

        User demoUser = new UserDAO().findByUsername("demo");
        assertNotNull(demoUser, "Setup: expected seeded demo user");
        demoUserId = demoUser.getId();
        songAId = getSongIdByTitle(SONG_A_TITLE);
        songBId = getSongIdByTitle(SONG_B_TITLE);
        songCId = getSongIdByTitle(SONG_C_TITLE);

        Playlist playlist = playlistDAO.createPlaylist(demoUserId, "IT-03 Remove Test", null);
        assertNotNull(playlist, "Setup: createPlaylist() must succeed");
        playlistId = playlist.getId();

        assertTrue(playlistDAO.addSongToPlaylist(playlistId, songAId), "Setup: add song A at pos 1");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, songBId), "Setup: add song B at pos 2");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, songCId), "Setup: add song C at pos 3");
    }

    @Test
    @Order(1)
    @DisplayName("IT-03-TB Step 1: removeSongFromPlaylist() returns true for song B")
    void testRemoveSong() {
        boolean removed = playlistDAO.removeSongFromPlaylist(playlistId, songBId);
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
                .anyMatch(e -> e.getSong().getId() == songBId);
        assertFalse(songBPresent, "Song B should not be in the playlist after removal");
    }

    @AfterAll
    static void tearDown() {
        if (playlistId > 0) {
            playlistDAO.deletePlaylist(playlistId);
        }
    }

    private static int getSongIdByTitle(String title) {
        return songDAO.search(title, SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC).stream()
            .filter(song -> title.equals(song.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Setup: expected seeded song " + title))
            .getId();
    }
}
