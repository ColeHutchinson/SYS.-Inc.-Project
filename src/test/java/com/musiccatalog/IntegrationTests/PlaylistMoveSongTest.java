package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.dao.UserDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.Song;
import com.musiccatalog.model.User;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-02-TB: PlaylistDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaylistMoveSongTest {

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

        // Create a fresh playlist and populate it with three songs in order A, B, C
        Playlist playlist = playlistDAO.createPlaylist(demoUserId, "IT-02 Move Test", null);
        assertNotNull(playlist, "Setup: createPlaylist() must succeed");
        playlistId = playlist.getId();

        assertTrue(playlistDAO.addSongToPlaylist(playlistId, songAId), "Setup: add song A");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, songBId), "Setup: add song B");
        assertTrue(playlistDAO.addSongToPlaylist(playlistId, songCId), "Setup: add song C");
    }

    @Test
    @Order(1)
    @DisplayName("IT-02-TB Step 1: initial order is A(pos1), B(pos2), C(pos3)")
    void testInitialOrder() {
        List<Song> songs = playlistDAO.getPlaylistWithSongs(playlistId).getSongs();
        assertEquals(3, songs.size());
        assertEquals(songAId, songs.get(0).getId());
        assertEquals(songBId, songs.get(1).getId());
        assertEquals(songCId, songs.get(2).getId());
    }

    @Test
    @Order(2)
    @DisplayName("IT-02-TB Step 2: moveSong() moves C from position 3 to position 1")
    void testMoveSong() {
        boolean moved = playlistDAO.moveSong(playlistId, songCId, 1);
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
        assertEquals(songCId, songs.get(0).getId(), "Song C should now be at position 1");
        assertEquals(songAId, songs.get(1).getId(), "Song A should now be at position 2");
        assertEquals(songBId, songs.get(2).getId(), "Song B should now be at position 3");
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
