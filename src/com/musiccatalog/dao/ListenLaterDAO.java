package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Listen Later operations.
 * Supports:
 * - add song to user's listen later list
 * - remove song from user's listen later list
 * - view all songs in user's listen later list
 * - check whether a song is already saved
 */
public class ListenLaterDAO {

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Adds a song to a user's Listen Later list.
     * Returns true if inserted, false if already exists or failed.
     */
    public boolean addSongToListenLater(int userId, int songId) {
        String sql = "INSERT OR IGNORE INTO listen_later (user_id, song_id) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding song to listen later: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes a song from a user's Listen Later list.
     * Returns true if removed.
     */
    public boolean removeSongFromListenLater(int userId, int songId) {
        String sql = "DELETE FROM listen_later WHERE user_id = ? AND song_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing song from listen later: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns all songs in the user's Listen Later list,
     * ordered by most recently added first.
     */
    public List<Song> getListenLaterSongs(int userId) {
        List<Song> songs = new ArrayList<>();

        String sql = """
            SELECT s.id, s.title, s.artist, s.album, s.duration_seconds,
                   s.genre, s.release_year, s.added_at
            FROM songs s
            INNER JOIN listen_later ll ON s.id = ll.song_id
            WHERE ll.user_id = ?
            ORDER BY ll.added_at DESC
        """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapSong(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching listen later songs: " + e.getMessage());
        }

        return songs;
    }

    /**
     * Checks whether a song is already in the user's Listen Later list.
     */
    public boolean isSongInListenLater(int userId, int songId) {
        String sql = "SELECT 1 FROM listen_later WHERE user_id = ? AND song_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, songId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking listen later entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the number of songs in a user's Listen Later list.
     */
    public int countListenLaterSongs(int userId) {
        String sql = "SELECT COUNT(*) FROM listen_later WHERE user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting listen later songs: " + e.getMessage());
        }
        return 0;
    }

    private Song mapSong(ResultSet rs) throws SQLException {
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
