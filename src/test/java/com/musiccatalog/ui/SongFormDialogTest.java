package com.musiccatalog.ui;

import com.musiccatalog.model.Song;
import com.musiccatalog.ui.SongFormDialog;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SongFormDialog.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SongFormDialogTest {

    private SongFormDialog dialog;

    @AfterEach
    public void teardown() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) dialog.dispose();
        });
    }

    // --- Helpers ---

    private Song song(int id, String title, String artist, String album,
                      int duration, String genre, int year) {
        return new Song(id, title, artist, album, duration, genre, year, "2022-01-01");
    }

    private void buildAddDialog() throws Exception {
        SwingUtilities.invokeAndWait(() ->
                dialog = new SongFormDialog(null, null)
        );
    }

    private void buildEditDialog(Song song) throws Exception {
        SwingUtilities.invokeAndWait(() ->
                dialog = new SongFormDialog(null, song)
        );
    }

    private void buildSuggestionDialog() throws Exception {
        SwingUtilities.invokeAndWait(() ->
                dialog = new SongFormDialog(null, null, "Suggest a Song", "Submit Suggestion", true)
        );
    }

    private void setFullFields(String title, String artist, String album,
                               String genre, String year, String mins, String secs) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            getField("titleField",   JTextField.class).setText(title);
            getField("artistField",  JTextField.class).setText(artist);
            getField("albumField",   JTextField.class).setText(album);
            getField("genreField",   JTextField.class).setText(genre);
            getField("yearField",    JTextField.class).setText(year);
            getField("minutesField", JTextField.class).setText(mins);
            getField("secondsField", JTextField.class).setText(secs);
        });
    }

    private void setSuggestionFields(String title, String artist) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            getField("titleField",  JTextField.class).setText(title);
            getField("artistField", JTextField.class).setText(artist);
        });
    }

    private void invokeDoSave(Song original) throws Exception {
        Method m = SongFormDialog.class.getDeclaredMethod("doSave", Song.class);
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(dialog, original);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String invokeValidationError() throws Exception {
        Method m = SongFormDialog.class.getDeclaredMethod("getValidationError");
        m.setAccessible(true);
        final String[] result = new String[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                result[0] = (String) m.invoke(dialog);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return result[0];
    }

    private <T> T getField(String name, Class<T> type) {
        try {
            Field f = SongFormDialog.class.getDeclaredField(name);
            f.setAccessible(true);
            return type.cast(f.get(dialog));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- getResult ---

    @Test
    public void testGetResultIsNullByDefault() throws Exception {
        buildAddDialog();
        assertNull(dialog.getResult(), "Result should be null before form is submitted");
    }

    // --- Add mode: successful saves ---

    @Test
    public void testAddSongReturnsCorrectResult() throws Exception {
        buildAddDialog();
        setFullFields("Bohemian Rhapsody", "Queen", "A Night at the Opera", "Rock", "1975", "5", "55");
        invokeDoSave(null);

        Song result = dialog.getResult();
        assertNotNull(result, "Result should not be null after valid save");
        assertEquals("Bohemian Rhapsody", result.getTitle());
        assertEquals("Queen",             result.getArtist());
        assertEquals("A Night at the Opera", result.getAlbum());
        assertEquals("Rock",              result.getGenre());
        assertEquals(1975,                result.getReleaseYear());
        assertEquals(355,                 result.getDurationSeconds()); // 5*60 + 55
    }

    @Test
    public void testAddSongWithIdZero() throws Exception {
        buildAddDialog();
        setFullFields("New Song", "Artist", "Album", "Pop", "2020", "3", "30");
        invokeDoSave(null);

        assertEquals(0, dialog.getResult().getId(), "New song should have ID 0");
    }

    @Test
    public void testAddSongEmptyAlbumAndGenreStoredAsNull() throws Exception {
        buildAddDialog();
        setFullFields("Track", "Artist", "", "", "2020", "3", "0");
        invokeDoSave(null);

        Song result = dialog.getResult();
        assertNull(result.getAlbum(), "Empty album should be stored as null");
        assertNull(result.getGenre(), "Empty genre should be stored as null");
    }

    @Test
    public void testAddSongEmptyYearStoredAsZero() throws Exception {
        buildAddDialog();
        setFullFields("Track", "Artist", "Album", "Pop", "", "2", "0");
        invokeDoSave(null);

        assertEquals(0, dialog.getResult().getReleaseYear(), "Empty year should be stored as 0");
    }

    @Test
    public void testDurationCalculatedCorrectly() throws Exception {
        buildAddDialog();
        setFullFields("Track", "Artist", "Album", "Pop", "2020", "4", "33");
        invokeDoSave(null);

        assertEquals(273, dialog.getResult().getDurationSeconds(), "Duration should be mins*60 + secs");
    }

    @Test
    public void testMinutesOnlyDuration() throws Exception {
        buildAddDialog();
        setFullFields("Track", "Artist", "Album", "Pop", "2020", "3", "0");
        invokeDoSave(null);

        assertEquals(180, dialog.getResult().getDurationSeconds(), "3 mins 0 secs should equal 180 seconds");
    }

    // --- Add mode: validation failures ---

    @Test
    public void testSaveWithEmptyTitleDoesNotSetResult() throws Exception {
        buildAddDialog();
        setFullFields("", "Artist", "Album", "Pop", "2020", "3", "30");
        assertEquals("Title and Artist are required.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when title is empty");
    }

    @Test
    public void testSaveWithEmptyArtistDoesNotSetResult() throws Exception {
        buildAddDialog();
        setFullFields("Title", "", "Album", "Pop", "2020", "3", "30");
        assertEquals("Title and Artist are required.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when artist is empty");
    }

    @Test
    public void testSaveWithZeroDurationDoesNotSetResult() throws Exception {
        buildAddDialog();
        setFullFields("Title", "Artist", "Album", "Pop", "2020", "0", "0");
        assertEquals("Please enter a valid duration.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when duration is 0");
    }

    @Test
    public void testSaveWithEmptyDurationDoesNotSetResult() throws Exception {
        buildAddDialog();
        setFullFields("Title", "Artist", "Album", "Pop", "2020", "", "");
        assertEquals("Please enter a valid duration.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when duration fields are empty");
    }

    @Test
    public void testSaveWithNonNumericDurationDoesNotSetResult() throws Exception {
        buildAddDialog();
        setFullFields("Title", "Artist", "Album", "Pop", "2020", "abc", "xyz");
        assertEquals("Please enter a valid duration.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when duration is non-numeric");
    }

    @Test
    public void testSaveWithNonNumericYearDoesNotSetResult() throws Exception {
        buildAddDialog();
        setFullFields("Title", "Artist", "Album", "Pop", "notayear", "3", "30");
        assertEquals("Please enter a valid year.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when year is non-numeric");
    }

    // --- Edit mode: prefill and ID preservation ---

    @Test
    public void testEditDialogPrefillsFields() throws Exception {
        Song existing = song(42, "Original Title", "Original Artist", "Original Album", 200, "Jazz", 2010);
        buildEditDialog(existing);

        assertEquals("Original Title",  getField("titleField",   JTextField.class).getText());
        assertEquals("Original Artist", getField("artistField",  JTextField.class).getText());
        assertEquals("Original Album",  getField("albumField",   JTextField.class).getText());
        assertEquals("Jazz",            getField("genreField",   JTextField.class).getText());
        assertEquals("2010",            getField("yearField",    JTextField.class).getText());
        assertEquals("3",               getField("minutesField", JTextField.class).getText()); // 200/60
        assertEquals("20",              getField("secondsField", JTextField.class).getText()); // 200%60
    }

    @Test
    public void testEditDialogPreservesOriginalId() throws Exception {
        Song existing = song(42, "Title", "Artist", "Album", 180, "Pop", 2020);
        buildEditDialog(existing);
        setFullFields("Updated Title", "Updated Artist", "Album", "Pop", "2020", "3", "0");
        invokeDoSave(existing);

        assertEquals(42, dialog.getResult().getId(), "Edit should preserve the original song's ID");
    }

    @Test
    public void testEditDialogUpdatesFields() throws Exception {
        Song existing = song(5, "Old Title", "Old Artist", "Album", 180, "Pop", 2019);
        buildEditDialog(existing);
        setFullFields("New Title", "New Artist", "New Album", "Rock", "2023", "4", "0");
        invokeDoSave(existing);

        Song result = dialog.getResult();
        assertEquals("New Title",  result.getTitle());
        assertEquals("New Artist", result.getArtist());
        assertEquals("New Album",  result.getAlbum());
        assertEquals("Rock",       result.getGenre());
        assertEquals(2023,         result.getReleaseYear());
        assertEquals(240,          result.getDurationSeconds());
    }

    @Test
    public void testEditDialogPrefillsNullAlbumAsEmpty() throws Exception {
        Song existing = song(1, "Title", "Artist", null, 180, null, 2020);
        buildEditDialog(existing);

        assertEquals("", getField("albumField", JTextField.class).getText(),
                "Null album should prefill as empty string");
        assertEquals("", getField("genreField", JTextField.class).getText(),
                "Null genre should prefill as empty string");
    }

    // --- Suggestion-only mode ---

    @Test
    public void testSuggestionOnlyModeHidesExtraFields() throws Exception {
        buildSuggestionDialog();

        // albumField, genreField, yearField, minutesField, secondsField are not built
        // in suggestionOnly mode — they remain null
        assertNull(getField("albumField",   JTextField.class),
                "Album field should not exist in suggestion-only mode");
        assertNull(getField("genreField",   JTextField.class),
                "Genre field should not exist in suggestion-only mode");
        assertNull(getField("yearField",    JTextField.class),
                "Year field should not exist in suggestion-only mode");
        assertNull(getField("minutesField", JTextField.class),
                "Minutes field should not exist in suggestion-only mode");
        assertNull(getField("secondsField", JTextField.class),
                "Seconds field should not exist in suggestion-only mode");
    }

    @Test
    public void testSuggestionOnlyModeAcceptsTitleAndArtistOnly() throws Exception {
        buildSuggestionDialog();
        setSuggestionFields("Suggest This", "Some Artist");
        invokeDoSave(null);

        Song result = dialog.getResult();
        assertNotNull(result, "Result should not be null after valid suggestion submit");
        assertEquals("Suggest This", result.getTitle());
        assertEquals("Some Artist",  result.getArtist());
    }

    @Test
    public void testSuggestionOnlyModeStoresNullForOptionalFields() throws Exception {
        buildSuggestionDialog();
        setSuggestionFields("Suggest This", "Some Artist");
        invokeDoSave(null);

        Song result = dialog.getResult();
        assertNull(result.getAlbum(),  "Album should be null in suggestion-only mode");
        assertNull(result.getGenre(),  "Genre should be null in suggestion-only mode");
        assertEquals(0, result.getDurationSeconds(), "Duration should be 0 in suggestion-only mode");
        assertEquals(0, result.getReleaseYear(),     "Year should be 0 in suggestion-only mode");
    }

    @Test
    public void testSuggestionOnlyModeWithEmptyTitleDoesNotSetResult() throws Exception {
        buildSuggestionDialog();
        setSuggestionFields("", "Some Artist");
        assertEquals("Title and Artist are required.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when title is empty in suggestion mode");
    }

    @Test
    public void testSuggestionOnlyModeWithEmptyArtistDoesNotSetResult() throws Exception {
        buildSuggestionDialog();
        setSuggestionFields("Some Title", "");
        assertEquals("Title and Artist are required.", invokeValidationError());

        assertNull(dialog.getResult(), "Result should be null when artist is empty in suggestion mode");
    }
}
