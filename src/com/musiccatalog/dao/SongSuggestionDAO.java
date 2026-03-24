package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.SongSuggestion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SongSuggestion operations.
 */
public class SongSuggestionDAO {

    public enum Status {
        PENDING
    }

    private Connection getConn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Add a new song suggestion.
     */
    public boolean addSuggestion(SongSuggestion suggestion) {
//        String sql = """
//            INSERT INTO song_suggestions (title, artist, album, duration_seconds, genre, release_year, suggested_by, status)
//            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
//        """;
//        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
//            ps.setString(1, suggestion.getTitle());
//            ps.setString(2, suggestion.getArtist());
//            ps.setString(3, suggestion.getAlbum());
//            ps.setInt(4, suggestion.getDurationSeconds());
//            ps.setString(5, suggestion.getGenre());
//            if (suggestion.getReleaseYear() != null) {
//                ps.setInt(6, suggestion.getReleaseYear());
//            } else {
//                ps.setNull(6, Types.INTEGER);
//            }
//            ps.setInt(7, suggestion.getSuggestedBy());
//            String status = suggestion.getStatus() != null ? suggestion.getStatus() : Status.PENDING.name();
//            ps.setString(8, status);
//            ps.executeUpdate();
//            return true;
//        } catch (SQLException e) {
//            System.err.println("Error adding suggestion: " + e.getMessage());
//            return false;
//        }
        return false;
    }

    /**
     * Get all suggestions with optional status filter.
     */
    public List<SongSuggestion> findAll(Status status) {
//        String sql = "SELECT * FROM song_suggestions";
//        if (status != null) {
//            sql += " WHERE status = ?";
//        }
//        sql += " ORDER BY suggested_at DESC";
//
//        List<SongSuggestion> suggestions = new ArrayList<>();
//        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
//            if (status != null) {
//                ps.setString(1, status.name());
//            }
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                suggestions.add(mapRow(rs));
//            }
//        } catch (SQLException e) {
//            System.err.println("Error finding suggestions: " + e.getMessage());
//        }
        return new ArrayList<>();
    }

    /**
     * Get suggestions by user.
     */
    public List<SongSuggestion> findByUser(int userId) {
//        String sql = "SELECT * FROM song_suggestions WHERE suggested_by = ? ORDER BY suggested_at DESC";
//        List<SongSuggestion> suggestions = new ArrayList<>();
//        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                suggestions.add(mapRow(rs));
//            }
//        } catch (SQLException e) {
//            System.err.println("Error finding user suggestions: " + e.getMessage());
//        }
        return new ArrayList<>();
    }

    /**
     * Delete a suggestion.
     */
    public boolean deleteSuggestion(int id) {
//        String sql = "DELETE FROM song_suggestions WHERE id = ?";
//        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
//            ps.setInt(1, id);
//            ps.executeUpdate();
//            return true;
//        } catch (SQLException e) {
//            System.err.println("Error deleting suggestion: " + e.getMessage());
//            return false;
//        }
        return false;
    }

    /**
     * Get suggestion by id.
     */
    public SongSuggestion findById(int id) {
//        String sql = "SELECT * FROM song_suggestions WHERE id = ?";
//        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
//            ps.setInt(1, id);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                return mapRow(rs);
//            }
//        } catch (SQLException e) {
//            System.err.println("Error finding suggestion: " + e.getMessage());
//        }
        return null;
    }

    private SongSuggestion mapRow(ResultSet rs) throws SQLException {
        return new SongSuggestion(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("artist"),
            rs.getString("album"),
            rs.getInt("duration_seconds"),
            rs.getString("genre"),
            getNullableInt(rs, "release_year"),
            rs.getInt("suggested_by"),
            rs.getString("status"),
            rs.getString("suggested_at"),
            rs.getString("reviewed_at"),
            getNullableInt(rs, "reviewed_by")
        );
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
