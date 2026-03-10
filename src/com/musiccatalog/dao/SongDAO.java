package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Song operations.
 * Supports searching, filtering, and sorting.
 */
public class SongDAO {

    public enum SortField {
        TITLE("title"),
        ARTIST("artist"),
        ALBUM("album"),
        DURATION("duration_seconds"),
        GENRE("genre"),
        YEAR("release_year"),
        DATE_ADDED("added_at");

        public final String column;
        SortField(String column) { this.column = column; }
    }

    public enum SortOrder {
        ASC, DESC
    }

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Get all songs with optional sorting.
     */
    public List<Song> findAll(SortField sortField, SortOrder sortOrder) {
        String sql = "SELECT * FROM songs ORDER BY " + sortField.column + " " + sortOrder.name();
        return executeQuery(sql);
    }

    /**
     * Search songs by keyword across title, artist, album, and genre.
     */
    public List<Song> search(String keyword, SortField sortField, SortOrder sortOrder) {
        String pattern = "%" + keyword.toLowerCase() + "%";
        String sql = """
            SELECT * FROM songs
            WHERE LOWER(title) LIKE ?
               OR LOWER(artist) LIKE ?
               OR LOWER(album) LIKE ?
               OR LOWER(genre) LIKE ?
            ORDER BY %s %s
        """.formatted(sortField.column, sortOrder.name());

        List<Song> songs = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                songs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching songs: " + e.getMessage());
        }
        return songs;
    }

    /**
     * Filter songs by genre.
     */
    public List<Song> findByGenre(String genre, SortField sortField, SortOrder sortOrder) {
        String sql = "SELECT * FROM songs WHERE LOWER(genre) = LOWER(?) ORDER BY " +
                     sortField.column + " " + sortOrder.name();
        List<Song> songs = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, genre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                songs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error filtering by genre: " + e.getMessage());
        }
        return songs;
    }

    /**
     * Filter songs by artist.
     */
    public List<Song> findByArtist(String artist, SortField sortField, SortOrder sortOrder) {
        String sql = "SELECT * FROM songs WHERE LOWER(artist) LIKE LOWER(?) ORDER BY " +
                     sortField.column + " " + sortOrder.name();
        List<Song> songs = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, "%" + artist + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                songs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error filtering by artist: " + e.getMessage());
        }
        return songs;
    }

    /**
     * Get all distinct genres for filter dropdown.
     */
    public List<String> findAllGenres() {
        List<String> genres = new ArrayList<>();
        genres.add("All Genres");
        String sql = "SELECT DISTINCT genre FROM songs WHERE genre IS NOT NULL ORDER BY genre";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching genres: " + e.getMessage());
        }
        return genres;
    }

    public Song findById(int id) {
        String sql = "SELECT * FROM songs WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error finding song: " + e.getMessage());
        }
        return null;
    }

    public boolean addSong(Song song) {
        String sql = """
            INSERT INTO songs (title, artist, album, duration_seconds, genre, release_year)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, song.getTitle());
            ps.setString(2, song.getArtist());
            ps.setString(3, song.getAlbum());
            ps.setInt(4, song.getDurationSeconds());
            ps.setString(5, song.getGenre());
            ps.setInt(6, song.getReleaseYear());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding song: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSong(Song song) {
        String sql = """
            UPDATE songs SET title=?, artist=?, album=?, duration_seconds=?, genre=?, release_year=?
            WHERE id=?
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, song.getTitle());
            ps.setString(2, song.getArtist());
            ps.setString(3, song.getAlbum());
            ps.setInt(4, song.getDurationSeconds());
            ps.setString(5, song.getGenre());
            ps.setInt(6, song.getReleaseYear());
            ps.setInt(7, song.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating song: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSong(int id) {
        String sql = "DELETE FROM songs WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting song: " + e.getMessage());
            return false;
        }
    }

    public int countAll() {
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM songs")) {
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }

    private List<Song> executeQuery(String sql) {
        List<Song> songs = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }
        return songs;
    }

    private Song mapRow(ResultSet rs) throws SQLException {
        return new Song(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("artist"),
            rs.getString("album"),
            rs.getInt("duration_seconds"),
            rs.getString("genre"),
            rs.getInt("release_year"),
            rs.getString("added_at")
        );
    }
}
