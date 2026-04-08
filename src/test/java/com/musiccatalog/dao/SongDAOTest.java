package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Song;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SongDAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SongDAOTest {

    private SongDAO dao;
    private Connection conn;

    @BeforeAll
    public void setupDatabase() throws SQLException {
        DatabaseManager.getInstance().initializeDatabase();
        conn = DatabaseManager.getInstance().getConnection();
        dao = new SongDAO();
    }

    @BeforeEach
    public void clearSongs() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM songs");
        }
    }

    @AfterAll
    public void cleanup() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    // --- Helpers ---

    private Song buildSong(String title, String artist, String album, int duration, String genre, int year) {
        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        song.setAlbum(album);
        song.setDurationSeconds(duration);
        song.setGenre(genre);
        song.setReleaseYear(year);
        return song;
    }

    // --- addSong ---

    @Test
    public void testAddSong() {
        Song song = buildSong("Test Song", "Test Artist", "Test Album", 180, "Pop", 2023);

        boolean result = dao.addSong(song);
        assertTrue(result, "Adding a song should succeed");

        List<Song> songs = dao.findAll(SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertFalse(songs.isEmpty(), "Song list should not be empty after adding");

        Song added = songs.get(0);
        assertEquals("Test Song", added.getTitle());
        assertEquals("Test Artist", added.getArtist());
        assertEquals("Pop", added.getGenre());
        assertEquals(2023, added.getReleaseYear());
    }

    // --- findAll ---

    @Test
    public void testFindAllSortedByTitleAsc() {
        dao.addSong(buildSong("Zebra Song", "Artist", "Album", 200, "Rock", 2020));
        dao.addSong(buildSong("Apple Song", "Artist", "Album", 200, "Rock", 2020));
        dao.addSong(buildSong("Mango Song", "Artist", "Album", 200, "Rock", 2020));

        List<Song> songs = dao.findAll(SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertEquals("Apple Song", songs.get(0).getTitle(), "First song should be alphabetically first");
        assertEquals("Zebra Song", songs.get(songs.size() - 1).getTitle(), "Last song should be alphabetically last");
    }

    @Test
    public void testFindAllSortedByYearDesc() {
        dao.addSong(buildSong("Old Song", "Artist", "Album", 200, "Jazz", 1990));
        dao.addSong(buildSong("New Song", "Artist", "Album", 200, "Jazz", 2022));

        List<Song> songs = dao.findAll(SongDAO.SortField.YEAR, SongDAO.SortOrder.DESC);
        assertEquals("New Song", songs.get(0).getTitle(), "Most recent song should appear first");
    }

    // --- search ---

    @Test
    public void testSearchByTitle() {
        dao.addSong(buildSong("Midnight Rain", "Taylor Swift", "Midnights", 200, "Pop", 2022));
        dao.addSong(buildSong("Shake It Off", "Taylor Swift", "1989", 220, "Pop", 2014));

        List<Song> results = dao.search("midnight", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertEquals(1, results.size(), "Search should return only matching songs");
        assertEquals("Midnight Rain", results.get(0).getTitle());
    }

    @Test
    public void testSearchByArtist() {
        dao.addSong(buildSong("Song A", "Radiohead", "OK Computer", 240, "Alternative", 1997));
        dao.addSong(buildSong("Song B", "Coldplay", "Parachutes", 210, "Alternative", 2000));

        List<Song> results = dao.search("radiohead", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertEquals(1, results.size(), "Search by artist name should match");
        assertEquals("Song A", results.get(0).getTitle());
    }

    @Test
    public void testSearchByGenre() {
        dao.addSong(buildSong("Blues Track", "Blues Artist", "Blues Album", 180, "Blues", 2000));
        dao.addSong(buildSong("Rock Track", "Rock Artist", "Rock Album", 200, "Rock", 2005));

        List<Song> results = dao.search("blues", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertEquals(1, results.size(), "Search by genre keyword should match");
    }

    @Test
    public void testSearchReturnsEmptyForNoMatch() {
        dao.addSong(buildSong("Known Song", "Known Artist", "Known Album", 200, "Pop", 2020));

        List<Song> results = dao.search("xyznotfound", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertNotNull(results, "Search result should not be null");
        assertTrue(results.isEmpty(), "Search should return empty list when no songs match");
    }

    @Test
    public void testSearchIsCaseInsensitive() {
        dao.addSong(buildSong("Summer Nights", "Artist", "Album", 200, "Pop", 2021));

        List<Song> lower = dao.search("summer nights", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        List<Song> upper = dao.search("SUMMER NIGHTS", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);

        assertEquals(1, lower.size(), "Lowercase search should match");
        assertEquals(1, upper.size(), "Uppercase search should match");
    }

    // --- findByGenre ---

    @Test
    public void testFindByGenre() {
        dao.addSong(buildSong("Jazz Track 1", "Artist", "Album", 200, "Jazz", 2010));
        dao.addSong(buildSong("Jazz Track 2", "Artist", "Album", 220, "Jazz", 2012));
        dao.addSong(buildSong("Pop Track", "Artist", "Album", 180, "Pop", 2015));

        List<Song> results = dao.findByGenre("Jazz", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertEquals(2, results.size(), "Should return only Jazz songs");
        assertTrue(results.stream().allMatch(s -> "Jazz".equalsIgnoreCase(s.getGenre())));
    }

    @Test
    public void testFindByGenreIsCaseInsensitive() {
        dao.addSong(buildSong("Rock Track", "Artist", "Album", 200, "Rock", 2000));

        List<Song> lower = dao.findByGenre("rock", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        List<Song> upper = dao.findByGenre("ROCK", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);

        assertEquals(1, lower.size(), "Lowercase genre filter should match");
        assertEquals(1, upper.size(), "Uppercase genre filter should match");
    }

    @Test
    public void testFindByGenreReturnsEmptyForUnknownGenre() {
        dao.addSong(buildSong("Some Song", "Artist", "Album", 200, "Pop", 2020));

        List<Song> results = dao.findByGenre("Bluegrass", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertNotNull(results, "Result should not be null");
        assertTrue(results.isEmpty(), "Should return empty list for unmatched genre");
    }

    // --- findByArtist ---

    @Test
    public void testFindByArtist() {
        dao.addSong(buildSong("Song 1", "The Beatles", "Abbey Road", 200, "Rock", 1969));
        dao.addSong(buildSong("Song 2", "The Beatles", "Let It Be", 210, "Rock", 1970));
        dao.addSong(buildSong("Song 3", "Elvis Presley", "Greatest Hits", 180, "Rock", 1960));

        List<Song> results = dao.findByArtist("Beatles", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertEquals(2, results.size(), "Should return all songs matching the artist");
    }

    @Test
    public void testFindByArtistReturnsEmptyForUnknownArtist() {
        dao.addSong(buildSong("Some Song", "Known Artist", "Album", 200, "Pop", 2020));

        List<Song> results = dao.findByArtist("Unknown Artist", SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        assertNotNull(results, "Result should not be null");
        assertTrue(results.isEmpty(), "Should return empty list for unmatched artist");
    }

    // --- findAllGenres ---

    @Test
    public void testFindAllGenres() {
        dao.addSong(buildSong("Song A", "Artist", "Album", 200, "Jazz", 2010));
        dao.addSong(buildSong("Song B", "Artist", "Album", 200, "Rock", 2015));
        dao.addSong(buildSong("Song C", "Artist", "Album", 200, "Jazz", 2018));

        List<String> genres = dao.findAllGenres();
        assertTrue(genres.contains("All Genres"), "Should always include 'All Genres' as first entry");
        assertTrue(genres.contains("Jazz"), "Should include Jazz genre");
        assertTrue(genres.contains("Rock"), "Should include Rock genre");
    }

    @Test
    public void testFindAllGenresNoDuplicates() {
        dao.addSong(buildSong("Song A", "Artist", "Album", 200, "Pop", 2020));
        dao.addSong(buildSong("Song B", "Artist", "Album", 200, "Pop", 2021));

        List<String> genres = dao.findAllGenres();
        long popCount = genres.stream().filter("Pop"::equals).count();
        assertEquals(1, popCount, "Each genre should appear only once");
    }

    // --- findById ---

    @Test
    public void testFindById() {
        dao.addSong(buildSong("Find Me", "Artist", "Album", 200, "Pop", 2020));

        List<Song> songs = dao.findAll(SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        Song added = songs.stream().filter(s -> "Find Me".equals(s.getTitle())).findFirst().orElse(null);
        assertNotNull(added, "Should find added song in list");

        Song found = dao.findById(added.getId());
        assertNotNull(found, "findById should return the song");
        assertEquals("Find Me", found.getTitle());
    }

    @Test
    public void testFindByIdReturnsNullForMissingId() {
        Song found = dao.findById(99999);
        assertNull(found, "findById should return null for a non-existent ID");
    }

    // --- updateSong ---

    @Test
    public void testUpdateSong() {
        dao.addSong(buildSong("Original Title", "Original Artist", "Album", 200, "Pop", 2020));

        List<Song> songs = dao.findAll(SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        Song added = songs.get(0);

        added.setTitle("Updated Title");
        added.setArtist("Updated Artist");
        added.setGenre("Rock");

        boolean result = dao.updateSong(added);
        assertTrue(result, "Update should succeed");

        Song updated = dao.findById(added.getId());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Artist", updated.getArtist());
        assertEquals("Rock", updated.getGenre());
    }

    // --- deleteSong ---

    @Test
    public void testDeleteSong() {
        dao.addSong(buildSong("Delete Me", "Artist", "Album", 200, "Pop", 2020));

        List<Song> songs = dao.findAll(SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        Song added = songs.stream().filter(s -> "Delete Me".equals(s.getTitle())).findFirst().orElse(null);
        assertNotNull(added, "Should find the song to delete");

        boolean deleted = dao.deleteSong(added.getId());
        assertTrue(deleted, "Deletion should succeed");

        Song gone = dao.findById(added.getId());
        assertNull(gone, "Deleted song should not be retrievable by ID");
    }

    // --- countAll ---

    @Test
    public void testCountAll() {
        assertEquals(0, dao.countAll(), "Count should be 0 on empty table");

        dao.addSong(buildSong("Song 1", "Artist", "Album", 200, "Pop", 2020));
        dao.addSong(buildSong("Song 2", "Artist", "Album", 200, "Pop", 2021));

        assertEquals(2, dao.countAll(), "Count should reflect the number of added songs");
    }

    @Test
    public void testCountDecreasesAfterDelete() {
        dao.addSong(buildSong("Song A", "Artist", "Album", 200, "Pop", 2020));
        dao.addSong(buildSong("Song B", "Artist", "Album", 200, "Pop", 2021));
        assertEquals(2, dao.countAll());

        List<Song> songs = dao.findAll(SongDAO.SortField.TITLE, SongDAO.SortOrder.ASC);
        dao.deleteSong(songs.get(0).getId());

        assertEquals(1, dao.countAll(), "Count should decrease by 1 after deletion");
    }
}