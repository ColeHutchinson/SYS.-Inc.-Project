package com.musiccatalog.IntegrationTests;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.dao.UserDAO;
import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.User;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IT-01-TB: PlaylistDAO + DatabaseManager
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlaylistCreateAndRetrieveTest {

    private static PlaylistDAO playlistDAO;
    private static int demoUserId;
    private static int createdPlaylistId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance().initializeDatabase();
        playlistDAO = new PlaylistDAO();
        User demoUser = new UserDAO().findByUsername("demo");
        assertNotNull(demoUser, "Setup: expected seeded demo user");
        demoUserId = demoUser.getId();
    }

    @Test
    @Order(1)
    @DisplayName("IT-01-TB Step 1: createPlaylist() returns a non-null Playlist with correct fields")
    void testCreatePlaylist() {
        Playlist playlist = playlistDAO.createPlaylist(demoUserId, "My Integration Test Playlist", "Created during IT-01");

        assertNotNull(playlist, "createPlaylist() should return a non-null Playlist");
        assertTrue(playlist.getId() > 0, "Created playlist should have a positive generated id");
        assertEquals(demoUserId, playlist.getUserId(), "Playlist user_id should match the demo user");
        assertEquals("My Integration Test Playlist", playlist.getName());
        assertEquals("Created during IT-01", playlist.getDescription());

        createdPlaylistId = playlist.getId();
    }

    @Test
    @Order(2)
    @DisplayName("IT-01-TB Step 2: getPlaylistsByUser() returns the created playlist — data persists")
    void testGetPlaylistsByUser() {
        List<Playlist> playlists = playlistDAO.getPlaylistsByUser(demoUserId);

        assertNotNull(playlists);
        assertFalse(playlists.isEmpty(), "getPlaylistsByUser() should return at least one playlist");

        boolean found = playlists.stream().anyMatch(p -> p.getId() == createdPlaylistId);
        assertTrue(found, "The playlist created in step 1 should be present in the user's playlists");
    }

    @AfterAll
    static void tearDown() {
        if (createdPlaylistId > 0) {
            playlistDAO.deletePlaylist(createdPlaylistId);
        }
    }
}
