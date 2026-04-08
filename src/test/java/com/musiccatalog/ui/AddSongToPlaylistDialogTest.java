package com.musiccatalog.ui;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.model.Song;
import com.musiccatalog.ui.AddSongToPlaylistDialog;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AddSongToPlaylistDialog.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddSongToPlaylistDialogTest {

    // --- Stubs ---

    /**
     * In-memory stub for PlaylistDAO.
     * Tracks which songIds are already in the playlist and records whether
     * addSongToPlaylist was ever called.
     */
    static class StubPlaylistDAO extends PlaylistDAO {
        private final Set<Integer> songsInPlaylist = new HashSet<>();
        boolean addWasCalled = false;
        int lastAddedSongId = -1;

        void markSongAsAlreadyInPlaylist(int songId) {
            songsInPlaylist.add(songId);
        }

        @Override
        public boolean containsSong(int playlistId, int songId) {
            return songsInPlaylist.contains(songId);
        }

        @Override
        public boolean addSongToPlaylist(int playlistId, int songId) {
            addWasCalled = true;
            lastAddedSongId = songId;
            songsInPlaylist.add(songId);
            return true;
        }
    }

    /**
     * Stub for SongDAO that returns a preset list from findAll(),
     * bypassing any DB access.
     */
    static class StubSongDAO extends SongDAO {
        private List<Song> songs = new ArrayList<>();

        void setSongs(List<Song> songs) {
            this.songs = songs;
        }

        @Override
        public List<Song> findAll(SortField sortField, SortOrder sortOrder) {
            return songs;
        }
    }

    // --- Test fields ---

    private StubPlaylistDAO stubPlaylistDAO;
    private StubSongDAO stubSongDAO;
    private AddSongToPlaylistDialog dialog;

    private static final int PLAYLIST_ID = 10;

    // --- Helpers ---

    private Song song(int id, String title, String artist, String album, int duration) {
        return new Song(id, title, artist, album, duration, "Pop", 2022, "2022-01-01");
    }

    @BeforeEach
    public void setup() throws Exception {
        stubPlaylistDAO = new StubPlaylistDAO();
        stubSongDAO = new StubSongDAO();

        SwingUtilities.invokeAndWait(() ->
                dialog = new AddSongToPlaylistDialog(null, stubPlaylistDAO, PLAYLIST_ID)
        );
    }

    @AfterEach
    public void teardown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) dialog.dispose();
        });
    }

    // --- wasAdded ---

    @Test
    public void testWasAddedIsFalseByDefault() {
        assertFalse(dialog.wasAdded(), "wasAdded should be false before any action is taken");
    }

    // --- Add button initial state ---

    @Test
    public void testAddButtonIsDisabledWithNoSelection() throws Exception {
        assertFalse(getAddButton().isEnabled(), "Add button should be disabled when no row is selected");
    }

    // --- Table model ---

    @Test
    public void testTableModelColumnCount() throws Exception {
        assertEquals(4, getSongTable().getColumnCount(), "Table should have 4 columns");
    }

    @Test
    public void testTableModelColumnNames() throws Exception {
        JTable table = getSongTable();
        assertEquals("Title",    table.getColumnName(0));
        assertEquals("Artist",   table.getColumnName(1));
        assertEquals("Album",    table.getColumnName(2));
        assertEquals("Duration", table.getColumnName(3));
    }

    @Test
    public void testTablePopulatesWithSongs() throws Exception {
        invokeLoadSongs(List.of(
                song(1, "Song A", "Artist A", "Album A", 180),
                song(2, "Song B", "Artist B", "Album B", 240)
        ), "");

        assertEquals(2, getSongTable().getRowCount(), "Table should show all loaded songs");
    }

    @Test
    public void testTableShowsCorrectValues() throws Exception {
        invokeLoadSongs(List.of(song(1, "Test Title", "Test Artist", "Test Album", 180)), "");

        JTable table = getSongTable();
        assertEquals("Test Title",  table.getValueAt(0, 0));
        assertEquals("Test Artist", table.getValueAt(0, 1));
        assertEquals("Test Album",  table.getValueAt(0, 2));
    }

    @Test
    public void testTableHandlesNullAlbum() throws Exception {
        invokeLoadSongs(List.of(new Song(1, "No Album Song", "Artist", null, 200, "Rock", 2021, "2021-01-01")), "");

        assertEquals("", getSongTable().getValueAt(0, 2), "Null album should render as empty string");
    }

    // --- Already-in-playlist marking ---

    @Test
    public void testSongAlreadyInPlaylistIsMarked() throws Exception {
        stubPlaylistDAO.markSongAsAlreadyInPlaylist(1);
        invokeLoadSongs(List.of(song(1, "Saved Song", "Artist", "Album", 200)), "");

        assertTrue(invokeIsAlreadyInPlaylist(getSongTable().getModel(), 0),
                "Song already in playlist should be flagged");
    }

    @Test
    public void testSongNotInPlaylistIsNotMarked() throws Exception {
        invokeLoadSongs(List.of(song(2, "New Song", "Artist", "Album", 200)), "");

        assertFalse(invokeIsAlreadyInPlaylist(getSongTable().getModel(), 0),
                "New song should not be flagged as already in playlist");
    }

    @Test
    public void testAddButtonDisabledForAlreadySavedSong() throws Exception {
        stubPlaylistDAO.markSongAsAlreadyInPlaylist(1);
        invokeLoadSongs(List.of(song(1, "Saved Song", "Artist", "Album", 200)), "");

        SwingUtilities.invokeAndWait(() -> {
            try {
                getSongTable().setRowSelectionInterval(0, 0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        SwingUtilities.invokeAndWait(() -> {});

        assertFalse(getAddButton().isEnabled(),
                "Add button should stay disabled when selected song is already in the playlist");
    }

    // --- doAdd ---

    @Test
    public void testDoAddCallsDAOAndSetsWasAdded() throws Exception {
        invokeLoadSongs(List.of(song(1, "New Song", "Artist", "Album", 200)), "");
        SwingUtilities.invokeAndWait(() -> {
            try {
                getSongTable().setRowSelectionInterval(0, 0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        invokeDoAdd();

        assertTrue(stubPlaylistDAO.addWasCalled, "addSongToPlaylist should have been called on the DAO");
        assertEquals(1, stubPlaylistDAO.lastAddedSongId, "Should have added the correct song ID");
        assertTrue(dialog.wasAdded(), "wasAdded should be true after a successful add");
    }

    @Test
    public void testDoAddMarksRowAsInPlaylist() throws Exception {
        invokeLoadSongs(List.of(song(1, "New Song", "Artist", "Album", 200)), "");
        SwingUtilities.invokeAndWait(() -> {
            try {
                getSongTable().setRowSelectionInterval(0, 0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        invokeDoAdd();

        assertTrue(invokeIsAlreadyInPlaylist(getSongTable().getModel(), 0),
                "Row should be marked as in-playlist after add");
    }

    @Test
    public void testDoAddDisablesAddButtonAfterAdd() throws Exception {
        invokeLoadSongs(List.of(song(1, "New Song", "Artist", "Album", 200)), "");
        SwingUtilities.invokeAndWait(() -> {
            try {
                getSongTable().setRowSelectionInterval(0, 0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        invokeDoAdd();

        assertFalse(getAddButton().isEnabled(), "Add button should be disabled after adding the song");
    }

    @Test
    public void testDoAddDoesNothingWhenNoRowSelected() throws Exception {
        invokeDoAdd();

        assertFalse(stubPlaylistDAO.addWasCalled,
                "addSongToPlaylist should not be called when no row is selected");
        assertFalse(dialog.wasAdded(), "wasAdded should remain false when no row is selected");
    }

    // --- Search filtering ---

    @Test
    public void testSearchFilterMatchesTitle() throws Exception {
        invokeLoadSongs(List.of(
                song(1, "Midnight Rain", "Artist A", "Album", 200),
                song(2, "Shake It Off", "Artist B", "Album", 200)
        ), "midnight");

        JTable table = getSongTable();
        assertEquals(1, table.getRowCount(), "Search by title should filter results");
        assertEquals("Midnight Rain", table.getValueAt(0, 0));
    }

    @Test
    public void testSearchFilterMatchesArtist() throws Exception {
        invokeLoadSongs(List.of(
                song(1, "Song A", "Radiohead", "Album", 200),
                song(2, "Song B", "Coldplay",  "Album", 200)
        ), "radiohead");

        assertEquals(1, getSongTable().getRowCount(), "Search by artist should filter results");
    }

    @Test
    public void testSearchFilterIsCaseInsensitive() throws Exception {
        invokeLoadSongs(List.of(song(1, "Summer Nights", "Artist", "Album", 200)), "SUMMER");

        assertEquals(1, getSongTable().getRowCount(), "Search filter should be case-insensitive");
    }

    @Test
    public void testSearchFilterReturnsEmptyForNoMatch() throws Exception {
        invokeLoadSongs(List.of(song(1, "Known Song", "Known Artist", "Album", 200)), "xyznotfound");

        assertEquals(0, getSongTable().getRowCount(), "Search with no match should return empty table");
    }

    @Test
    public void testEmptySearchShowsAllSongs() throws Exception {
        invokeLoadSongs(List.of(
                song(1, "Song A", "Artist A", "Album", 200),
                song(2, "Song B", "Artist B", "Album", 200),
                song(3, "Song C", "Artist C", "Album", 200)
        ), "");

        assertEquals(3, getSongTable().getRowCount(), "Empty search string should show all songs");
    }

    // --- Reflection helpers ---

    private JTable getSongTable() throws Exception {
        Field f = AddSongToPlaylistDialog.class.getDeclaredField("songTable");
        f.setAccessible(true);
        return (JTable) f.get(dialog);
    }

    private JButton getAddButton() throws Exception {
        Field f = AddSongToPlaylistDialog.class.getDeclaredField("addBtn");
        f.setAccessible(true);
        return (JButton) f.get(dialog);
    }

    /**
     * Injects the StubSongDAO with the given song list, then calls
     * loadSongs(filter) via reflection on the EDT.
     */
    private void invokeLoadSongs(List<Song> songs, String filter) throws Exception {
        stubSongDAO.setSongs(songs);

        Field daoField = AddSongToPlaylistDialog.class.getDeclaredField("songDAO");
        daoField.setAccessible(true);

        Method loadSongs = AddSongToPlaylistDialog.class.getDeclaredMethod("loadSongs", String.class);
        loadSongs.setAccessible(true);

        SwingUtilities.invokeAndWait(() -> {
            try {
                daoField.set(dialog, stubSongDAO);
                loadSongs.invoke(dialog, filter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void invokeDoAdd() throws Exception {
        Method doAdd = AddSongToPlaylistDialog.class.getDeclaredMethod("doAdd");
        doAdd.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                doAdd.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean invokeIsAlreadyInPlaylist(Object tableModel, int row) throws Exception {
        Method m = tableModel.getClass().getDeclaredMethod("isAlreadyInPlaylist", int.class);
        m.setAccessible(true);
        return (boolean) m.invoke(tableModel, row);
    }
}