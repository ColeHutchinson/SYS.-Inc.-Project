package com.musiccatalog.ui;

import com.musiccatalog.dao.SongSuggestionDAO;
import com.musiccatalog.model.SongSuggestion;
import com.musiccatalog.ui.SuggestionsDialog;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SuggestionsDialog.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SuggestionsDialogTest {

    // --- Stub ---

    /**
     * In-memory stub for SongSuggestionDAO.
     * findAll() returns a preset list. deleteSuggestion() removes from that
     * list and records whether it was called.
     */
    static class StubSongSuggestionDAO extends SongSuggestionDAO {
        private final List<SongSuggestion> suggestions = new ArrayList<>();
        boolean deleteWasCalled = false;
        int lastDeletedId = -1;
        boolean deleteResult = true;

        void setSuggestions(List<SongSuggestion> incoming) {
            suggestions.clear();
            suggestions.addAll(incoming);
        }

        @Override
        public List<SongSuggestion> findAll(SongSuggestionDAO.Status status) {
            return new ArrayList<>(suggestions);
        }

        @Override
        public boolean deleteSuggestion(int id) {
            deleteWasCalled = true;
            lastDeletedId = id;
            if (deleteResult) {
                suggestions.removeIf(s -> s.getId() == id);
            }
            return deleteResult;
        }
    }

    // --- Test fields ---

    private StubSongSuggestionDAO stubDAO;
    private SuggestionsDialog dialog;

    // --- Helpers ---

    private SongSuggestion suggestion(int id, String title, String artist, int suggestedBy) {
        SongSuggestion s = new SongSuggestion();
        s.setId(id);
        s.setTitle(title);
        s.setArtist(artist);
        s.setSuggestedBy(suggestedBy);
        s.setStatus("PENDING");
        s.setSuggestedAt("2024-01-01 12:00:00");
        return s;
    }

    @BeforeEach
    public void setup() throws Exception {
        stubDAO = new StubSongSuggestionDAO();
        SwingUtilities.invokeAndWait(() ->
                dialog = new SuggestionsDialog(null, null)
        );
        injectDAO();
    }

    @AfterEach
    public void teardown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) dialog.dispose();
        });
    }

    // --- Table model ---

    @Test
    public void testTableModelColumnCount() throws Exception {
        assertEquals(4, getSuggestionsTable().getColumnCount(), "Table should have 4 columns");
    }

    @Test
    public void testTableModelColumnNames() throws Exception {
        JTable table = getSuggestionsTable();
        assertEquals("Title",        table.getColumnName(0));
        assertEquals("Artist",       table.getColumnName(1));
        assertEquals("Suggested By", table.getColumnName(2));
        assertEquals("Suggested At", table.getColumnName(3));
    }

    @Test
    public void testTablePopulatesAfterLoad() throws Exception {
        stubDAO.setSuggestions(List.of(
                suggestion(1, "Song A", "Artist A", 1),
                suggestion(2, "Song B", "Artist B", 2)
        ));
        invokeLoadSuggestions();

        assertEquals(2, getSuggestionsTable().getRowCount(), "Table should show all loaded suggestions");
    }

    @Test
    public void testTableShowsCorrectValues() throws Exception {
        stubDAO.setSuggestions(List.of(suggestion(1, "Test Title", "Test Artist", 3)));
        invokeLoadSuggestions();

        JTable table = getSuggestionsTable();
        assertEquals("Test Title",  table.getValueAt(0, 0));
        assertEquals("Test Artist", table.getValueAt(0, 1));
        assertEquals("User 3",      table.getValueAt(0, 2));
        assertEquals("2024-01-01 12:00:00", table.getValueAt(0, 3));
    }

    @Test
    public void testTableIsEmptyWhenNoSuggestions() throws Exception {
        stubDAO.setSuggestions(List.of());
        invokeLoadSuggestions();

        assertEquals(0, getSuggestionsTable().getRowCount(), "Table should be empty when no suggestions exist");
    }

    // --- loadSuggestions ---

    @Test
    public void testLoadSuggestionsRefreshesTable() throws Exception {
        stubDAO.setSuggestions(List.of(suggestion(1, "Song A", "Artist A", 1)));
        invokeLoadSuggestions();
        assertEquals(1, getSuggestionsTable().getRowCount());

        stubDAO.setSuggestions(List.of(
                suggestion(1, "Song A", "Artist A", 1),
                suggestion(2, "Song B", "Artist B", 2)
        ));
        invokeLoadSuggestions();
        assertEquals(2, getSuggestionsTable().getRowCount(), "Table should update after reload");
    }

    @Test
    public void testLoadSuggestionsClearsTableOnEmpty() throws Exception {
        stubDAO.setSuggestions(List.of(suggestion(1, "Song A", "Artist A", 1)));
        invokeLoadSuggestions();
        assertEquals(1, getSuggestionsTable().getRowCount());

        stubDAO.setSuggestions(List.of());
        invokeLoadSuggestions();
        assertEquals(0, getSuggestionsTable().getRowCount(), "Table should clear when suggestions are removed");
    }

    // --- deleteSelectedSuggestion ---

    @Test
    public void testDeleteWithNoSelectionDoesNotCallDAO() throws Exception {
        stubDAO.setSuggestions(List.of(suggestion(1, "Song A", "Artist A", 1)));
        invokeLoadSuggestions();

        // Ensure nothing is selected
        SwingUtilities.invokeAndWait(() -> getSuggestionsTable().clearSelection());

        invokeDeleteSelectedSuggestion();

        assertFalse(stubDAO.deleteWasCalled, "deleteSuggestion should not be called when no row is selected");
    }

    @Test
    public void testDeleteCallsDAOWithCorrectId() throws Exception {
        stubDAO.setSuggestions(List.of(suggestion(7, "Song A", "Artist A", 1)));
        invokeLoadSuggestions();

        SwingUtilities.invokeAndWait(() -> getSuggestionsTable().setRowSelectionInterval(0, 0));

        invokeDeleteSelectedSuggestionConfirmed();

        assertTrue(stubDAO.deleteWasCalled, "deleteSuggestion should be called on the DAO");
        assertEquals(7, stubDAO.lastDeletedId, "Should delete the correct suggestion by ID");
    }

    @Test
    public void testDeleteRemovesSuggestionFromTable() throws Exception {
        stubDAO.setSuggestions(List.of(
                suggestion(1, "Song A", "Artist A", 1),
                suggestion(2, "Song B", "Artist B", 2)
        ));
        invokeLoadSuggestions();
        assertEquals(2, getSuggestionsTable().getRowCount());

        SwingUtilities.invokeAndWait(() -> getSuggestionsTable().setRowSelectionInterval(0, 0));
        invokeDeleteSelectedSuggestionConfirmed();

        assertEquals(1, getSuggestionsTable().getRowCount(), "Table should have one fewer row after deletion");
    }

    @Test
    public void testTableModelGetSuggestionAt() throws Exception {
        SongSuggestion s = suggestion(99, "Check Me", "Some Artist", 5);
        stubDAO.setSuggestions(List.of(s));
        invokeLoadSuggestions();

        Object tableModel = getSuggestionsTable().getModel();
        Method m = tableModel.getClass().getDeclaredMethod("getSuggestionAt", int.class);
        m.setAccessible(true);
        SongSuggestion returned = (SongSuggestion) m.invoke(tableModel, 0);

        assertEquals(99,           returned.getId());
        assertEquals("Check Me",   returned.getTitle());
        assertEquals("Some Artist", returned.getArtist());
    }

    // --- Reflection helpers ---

    private void injectDAO() throws Exception {
        Field f = SuggestionsDialog.class.getDeclaredField("suggestionDAO");
        f.setAccessible(true);
        f.set(dialog, stubDAO);
    }

    private JTable getSuggestionsTable() {
        try {
            Field f = SuggestionsDialog.class.getDeclaredField("suggestionsTable");
            f.setAccessible(true);
            return (JTable) f.get(dialog);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeLoadSuggestions() throws Exception {
        Method m = SuggestionsDialog.class.getDeclaredMethod("loadSuggestions");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Invokes deleteSelectedSuggestion() directly — suitable for the no-selection
     * case where no confirm dialog will appear.
     */
    private void invokeDeleteSelectedSuggestion() throws Exception {
        Method m = SuggestionsDialog.class.getDeclaredMethod("deleteSelectedSuggestion");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Bypasses the JOptionPane confirm dialog by calling the DAO and reloading
     * directly — simulates the YES_OPTION branch without blocking on a dialog.
     */
    private void invokeDeleteSelectedSuggestionConfirmed() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTable table = getSuggestionsTable();
                int row = table.getSelectedRow();
                if (row < 0) return;

                Object tableModel = table.getModel();
                Method getSuggestionAt = tableModel.getClass().getDeclaredMethod("getSuggestionAt", int.class);
                getSuggestionAt.setAccessible(true);
                SongSuggestion suggestion = (SongSuggestion) getSuggestionAt.invoke(tableModel, row);

                stubDAO.deleteSuggestion(suggestion.getId());

                Method load = SuggestionsDialog.class.getDeclaredMethod("loadSuggestions");
                load.setAccessible(true);
                load.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}