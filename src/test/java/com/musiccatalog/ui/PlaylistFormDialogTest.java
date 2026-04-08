package com.musiccatalog.ui;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.ui.PlaylistFormDialog;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlaylistFormDialog.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlaylistFormDialogTest {

    // --- Stub ---

    /**
     * In-memory stub for PlaylistDAO.
     * Records arguments passed to createPlaylist() and updatePlaylist(),
     * and returns preset results.
     */
    static class StubPlaylistDAO extends PlaylistDAO {
        // createPlaylist controls
        Playlist createResult = null;
        boolean createWasCalled = false;
        String lastCreatedName = null;
        String lastCreatedDescription = null;

        // updatePlaylist controls
        boolean updateResult = true;
        boolean updateWasCalled = false;
        String lastUpdatedName = null;
        String lastUpdatedDescription = null;

        // getPlaylistById control
        Playlist getByIdResult = null;

        @Override
        public Playlist createPlaylist(int userId, String name, String description) {
            createWasCalled = true;
            lastCreatedName = name;
            lastCreatedDescription = description;
            return createResult;
        }

        @Override
        public boolean updatePlaylist(int playlistId, String name, String description) {
            updateWasCalled = true;
            lastUpdatedName = name;
            lastUpdatedDescription = description;
            return updateResult;
        }

        @Override
        public Playlist getPlaylistById(int playlistId) {
            return getByIdResult;
        }
    }

    // --- Test fields ---

    private StubPlaylistDAO stubPlaylistDAO;
    private PlaylistFormDialog dialog;

    private static final int USER_ID = 1;

    // --- Helpers ---

    private Playlist playlist(int id, String name, String description) {
        return new Playlist(id, USER_ID, name, description, "2024-01-01");
    }

    private void openCreateDialog() throws Exception {
        SwingUtilities.invokeAndWait(() ->
                dialog = new PlaylistFormDialog(null, stubPlaylistDAO, USER_ID)
        );
    }

    private void openEditDialog(Playlist existing) throws Exception {
        SwingUtilities.invokeAndWait(() ->
                dialog = new PlaylistFormDialog(null, stubPlaylistDAO, USER_ID, existing)
        );
    }

    @BeforeEach
    public void setup() throws Exception {
        stubPlaylistDAO = new StubPlaylistDAO();
    }

    @AfterEach
    public void teardown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) dialog.dispose();
        });
    }

    // --- getResult ---

    @Test
    public void testGetResultIsNullByDefault() throws Exception {
        openCreateDialog();
        assertNull(dialog.getResult(), "Result should be null before save is called");
    }

    // --- Create mode: field prefill ---

    @Test
    public void testCreateModeFieldsAreEmptyByDefault() throws Exception {
        openCreateDialog();

        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("", getField("nameField", JTextField.class).getText(),
                "Name field should be empty in create mode");
        assertEquals("", getField("descriptionArea", JTextArea.class).getText(),
                "Description field should be empty in create mode");
    }

    // --- Edit mode: field prefill ---

    @Test
    public void testEditModePreFillsNameField() throws Exception {
        openEditDialog(playlist(1, "My Playlist", "A description"));

        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("My Playlist", getField("nameField", JTextField.class).getText(),
                "Name field should be prefilled with existing playlist name");
    }

    @Test
    public void testEditModePreFillsDescriptionField() throws Exception {
        openEditDialog(playlist(1, "My Playlist", "A description"));

        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("A description", getField("descriptionArea", JTextArea.class).getText(),
                "Description field should be prefilled with existing playlist description");
    }

    @Test
    public void testEditModeHandlesNullDescription() throws Exception {
        openEditDialog(playlist(1, "My Playlist", null));

        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("", getField("descriptionArea", JTextArea.class).getText(),
                "Description field should be empty when existing playlist has null description");
    }

    // --- doSave: empty name validation ---

    @Test
    public void testSaveWithEmptyNameDoesNotCallDAO() throws Exception {
        openCreateDialog();
        setFields("", "");
        invokeDoSave();

        assertFalse(stubPlaylistDAO.createWasCalled, "createPlaylist() should not be called when name is empty");
        assertNull(dialog.getResult(), "Result should remain null when validation fails");
    }

    @Test
    public void testSaveWithWhitespaceOnlyNameDoesNotCallDAO() throws Exception {
        openCreateDialog();
        setFields("   ", "Some description");
        invokeDoSave();

        assertFalse(stubPlaylistDAO.createWasCalled, "createPlaylist() should not be called when name is only whitespace");
        assertNull(dialog.getResult(), "Result should remain null when validation fails");
    }

    // --- doSave: create mode ---

    @Test
    public void testSaveInCreateModeCallsCreatePlaylist() throws Exception {
        Playlist created = playlist(1, "New Playlist", "A description");
        stubPlaylistDAO.createResult = created;

        openCreateDialog();
        setFields("New Playlist", "A description");
        invokeDoSave();

        assertTrue(stubPlaylistDAO.createWasCalled, "createPlaylist() should be called on save in create mode");
        assertEquals("New Playlist",  stubPlaylistDAO.lastCreatedName);
        assertEquals("A description", stubPlaylistDAO.lastCreatedDescription);
    }

    @Test
    public void testSaveInCreateModeReturnsCreatedPlaylist() throws Exception {
        Playlist created = playlist(1, "New Playlist", "A description");
        stubPlaylistDAO.createResult = created;

        openCreateDialog();
        setFields("New Playlist", "A description");
        invokeDoSave();

        assertNotNull(dialog.getResult(), "Result should be the created playlist after save");
        assertEquals("New Playlist", dialog.getResult().getName());
    }

    @Test
    public void testSaveInCreateModeWithEmptyDescriptionPassesNull() throws Exception {
        Playlist created = playlist(1, "No Desc Playlist", null);
        stubPlaylistDAO.createResult = created;

        openCreateDialog();
        setFields("No Desc Playlist", "");
        invokeDoSave();

        assertNull(stubPlaylistDAO.lastCreatedDescription,
                "Empty description should be passed as null to createPlaylist()");
    }

    @Test
    public void testSaveInCreateModeDoesNotCallUpdate() throws Exception {
        stubPlaylistDAO.createResult = playlist(1, "New Playlist", null);

        openCreateDialog();
        setFields("New Playlist", "");
        invokeDoSave();

        assertFalse(stubPlaylistDAO.updateWasCalled, "updatePlaylist() should not be called in create mode");
    }

    // --- doSave: edit mode ---

    @Test
    public void testSaveInEditModeCallsUpdatePlaylist() throws Exception {
        Playlist existing = playlist(5, "Old Name", "Old desc");
        stubPlaylistDAO.updateResult = true;
        stubPlaylistDAO.getByIdResult = playlist(5, "Updated Name", "New desc");

        openEditDialog(existing);
        setFields("Updated Name", "New desc");
        invokeDoSave();

        assertTrue(stubPlaylistDAO.updateWasCalled, "updatePlaylist() should be called on save in edit mode");
        assertEquals("Updated Name", stubPlaylistDAO.lastUpdatedName);
        assertEquals("New desc",     stubPlaylistDAO.lastUpdatedDescription);
    }

    @Test
    public void testSaveInEditModeReturnsUpdatedPlaylist() throws Exception {
        Playlist existing = playlist(5, "Old Name", "Old desc");
        Playlist updated  = playlist(5, "Updated Name", "New desc");
        stubPlaylistDAO.updateResult   = true;
        stubPlaylistDAO.getByIdResult  = updated;

        openEditDialog(existing);
        setFields("Updated Name", "New desc");
        invokeDoSave();

        assertNotNull(dialog.getResult(), "Result should be set after a successful update");
        assertEquals("Updated Name", dialog.getResult().getName());
    }

    @Test
    public void testSaveInEditModeWhenUpdateFailsResultIsNull() throws Exception {
        Playlist existing = playlist(5, "Old Name", "Old desc");
        stubPlaylistDAO.updateResult  = false;
        stubPlaylistDAO.getByIdResult = null;

        openEditDialog(existing);
        setFields("Updated Name", "New desc");
        invokeDoSave();

        assertNull(dialog.getResult(), "Result should remain null when updatePlaylist() returns false");
    }

    @Test
    public void testSaveInEditModeWithEmptyDescriptionPassesNull() throws Exception {
        Playlist existing = playlist(5, "Playlist", "Old desc");
        stubPlaylistDAO.updateResult  = true;
        stubPlaylistDAO.getByIdResult = playlist(5, "Playlist", null);

        openEditDialog(existing);
        setFields("Playlist", "");
        invokeDoSave();

        assertNull(stubPlaylistDAO.lastUpdatedDescription,
                "Empty description should be passed as null to updatePlaylist()");
    }

    @Test
    public void testSaveInEditModeDoesNotCallCreate() throws Exception {
        Playlist existing = playlist(5, "Old Name", "Old desc");
        stubPlaylistDAO.updateResult  = true;
        stubPlaylistDAO.getByIdResult = playlist(5, "Updated Name", null);

        openEditDialog(existing);
        setFields("Updated Name", "");
        invokeDoSave();

        assertFalse(stubPlaylistDAO.createWasCalled, "createPlaylist() should not be called in edit mode");
    }

    // --- Reflection helpers ---

    private <T> T getField(String name, Class<T> type) {
        try {
            Field f = PlaylistFormDialog.class.getDeclaredField(name);
            f.setAccessible(true);
            return type.cast(f.get(dialog));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setFields(String name, String description) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            getField("nameField",       JTextField.class).setText(name);
            getField("descriptionArea", JTextArea.class).setText(description);
        });
    }

    private void invokeDoSave() throws Exception {
        Method m = PlaylistFormDialog.class.getDeclaredMethod("doSave");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}