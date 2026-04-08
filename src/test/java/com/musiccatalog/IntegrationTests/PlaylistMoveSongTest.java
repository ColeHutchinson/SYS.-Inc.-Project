package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.Song;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-02-TB: PlaylistDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaylistMoveSongTest {

    private static final int DEMO_USER_ID = 2;
    // Song ids from the seeded songs table
    private static final int SONG_A = 1;
    private static final int SONG_B = 2;
    private static final int SONG_C = 3;

    private static PlaylistDAO playlistDAO;
    private static int playlistId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        playlistDAO = new PlaylistDAO();

        // Create a fresh playlist and populate it with three songs in order A, B, C
        Playlist playlist = playlistDAO.createPlaylist(DEMO_USER_ID, "IT-02 Move Test", null);
        assertNotNull(playlist, "Setup: createPlaylist() must succeed");
        playlistId = playlist.getId();

        assertTrue(playlistDAO.addSongToPlaylist(playlistId, SONG_A), "Setup: add song A");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, SONG_B), "Setup: add song B");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, SONG_C), "Setup: add song C");
    }

    @Test
    @Order(1)
    @DisplayName("IT-02-TB Step 1: initial order is A(pos1), B(pos2), C(pos3)")
    void testInitialOrder() {
        List<Song> songs = playlistDAO.getPlaylistWithSongs(playlistId).getSongs();
        assertEquals(3, songs.size());
        assertEquals(SONG_A, songs.get(0).getId());
        assertEquals(SONG_B, songs.get(1).getId());
        assertEquals(SONG_C, songs.get(2).getId());
    }

    @Test
    @Order(2)
    @DisplayName("IT-02-TB Step 2: moveSong() moves C from position 3 to position 1")
    void testMoveSong() {
        boolean moved = playlistDAO.moveSong(playlistId, SONG_C, 1);
        assertTrue(moved, "moveSong() should return true on success");
    }

    @Test
    @Order(3)
    @DisplayName("IT-02-TB Step 3: getPlaylistWithSongs() reflects new order C(pos1), A(pos2), B(pos3)")
    void testOrderAfterMove() {
        Playlist playlist = playlistDAO.getPlaylistWithSongs(playlistId);
        assertNotNull(playlist);

        List<Song> songs = playlist.getSongs();
        assertEquals(3, songs.size(), "Playlist should still have 3 songs after move");
        assertEquals(SONG_C, songs.get(0).getId(), "Song C should now be at position 1");
        assertEquals(SONG_A, songs.get(1).getId(), "Song A should now be at position 2");
        assertEquals(SONG_B, songs.get(2).getId(), "Song B should now be at position 3");
    }

    @AfterAll
    static void tearDown() {
        if (playlistId > 0) {
            playlistDAO.deletePlaylist(playlistId);
        }
    }
}
