package com.musiccatalog.ui;

import com.musiccatalog.dao.ListenLaterDAO;
import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.model.Song;
import com.musiccatalog.ui.AddSongToListenLaterDialog;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AddSongToListenLaterDialog.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddSongToListenLaterDialogTest {

    // --- Stubs ---

    /**
     * In-memory stub for ListenLaterDAO.
     * Tracks which (userId, songId) pairs are saved and records whether
     * addSongToListenLater was ever called.
     */
    static class StubListenLaterDAO extends ListenLaterDAO {
        private final Set<Integer> savedSongIds = new HashSet<>();
        boolean addWasCalled = false;
        int lastAddedSongId = -1;

        void markSongAsAlreadySaved(int songId) {
            savedSongIds.add(songId);
        }

        @Override
        public boolean isSongInListenLater(int userId, int songId) {
            return savedSongIds.contains(songId);
        }

        @Override
        public boolean addSongToListenLater(int userId, int songId) {
            addWasCalled = true;
            lastAddedSongId = songId;
            savedSongIds.add(songId);
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

    private StubListenLaterDAO stubListenLaterDAO;
    private StubSongDAO stubSongDAO;
    private AddSongToListenLaterDialog dialog;

    private static final int USER_ID = 1;

    // --- Helpers ---

    private Song song(int id, String title, String artist, String album, int duration) {
        return new Song(id, title, artist, album, duration, "Pop", 2022, "2022-01-01");
    }

    @BeforeEach
    public void setup() throws Exception {
        stubListenLaterDAO = new StubListenLaterDAO();
        stubSongDAO = new StubSongDAO();

        SwingUtilities.invokeAndWait(() ->
                dialog = new AddSongToListenLaterDialog(null, stubListenLaterDAO, USER_ID)
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

    // --- Already-in-list marking ---

    @Test
    public void testSongAlreadyInListIsMarked() throws Exception {
        stubListenLaterDAO.markSongAsAlreadySaved(1);
        invokeLoadSongs(List.of(song(1, "Saved Song", "Artist", "Album", 200)), "");

        assertTrue(invokeIsAlreadyInList(getSongTable().getModel(), 0),
                "Song already in list should be flagged");
    }

    @Test
    public void testSongNotInListIsNotMarked() throws Exception {
        invokeLoadSongs(List.of(song(2, "New Song", "Artist", "Album", 200)), "");

        assertFalse(invokeIsAlreadyInList(getSongTable().getModel(), 0),
                "New song should not be flagged as already in list");
    }

    @Test
    public void testAddButtonDisabledForAlreadySavedSong() throws Exception {
        stubListenLaterDAO.markSongAsAlreadySaved(1);
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
                "Add button should stay disabled when selected song is already in the list");
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

        assertTrue(stubListenLaterDAO.addWasCalled, "addSongToListenLater should have been called on the DAO");
        assertEquals(1, stubListenLaterDAO.lastAddedSongId, "Should have added the correct song ID");
        assertTrue(dialog.wasAdded(), "wasAdded should be true after a successful add");
    }

    @Test
    public void testDoAddMarksRowAsInList() throws Exception {
        invokeLoadSongs(List.of(song(1, "New Song", "Artist", "Album", 200)), "");
        SwingUtilities.invokeAndWait(() -> {
            try {
                getSongTable().setRowSelectionInterval(0, 0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        invokeDoAdd();

        assertTrue(invokeIsAlreadyInList(getSongTable().getModel(), 0),
                "Row should be marked as in-list after add");
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

        assertFalse(stubListenLaterDAO.addWasCalled,
                "addSongToListenLater should not be called when no row is selected");
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
        Field f = AddSongToListenLaterDialog.class.getDeclaredField("songTable");
        f.setAccessible(true);
        return (JTable) f.get(dialog);
    }

    private JButton getAddButton() throws Exception {
        Field f = AddSongToListenLaterDialog.class.getDeclaredField("addBtn");
        f.setAccessible(true);
        return (JButton) f.get(dialog);
    }

    /**
     * Injects the StubSongDAO with the given song list, then calls
     * loadSongs(filter) via reflection on the EDT.
     */
    private void invokeLoadSongs(List<Song> songs, String filter) throws Exception {
        stubSongDAO.setSongs(songs);

        Field daoField = AddSongToListenLaterDialog.class.getDeclaredField("songDAO");
        daoField.setAccessible(true);

        Method loadSongs = AddSongToListenLaterDialog.class.getDeclaredMethod("loadSongs", String.class);
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
        Method doAdd = AddSongToListenLaterDialog.class.getDeclaredMethod("doAdd");
        doAdd.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                doAdd.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean invokeIsAlreadyInList(Object tableModel, int row) throws Exception {
        Method m = tableModel.getClass().getDeclaredMethod("isAlreadyInList", int.class);
        m.setAccessible(true);
        return (boolean) m.invoke(tableModel, row);
    }
}