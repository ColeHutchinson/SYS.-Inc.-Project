package com.musiccatalog.dao;

import com.musiccatalog.db.DatabaseManager;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.PlaylistSong;
import com.musiccatalog.model.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 Data Access Object for playlist operations.
 Covers:
   Playlist CRUD (create, read, update, delete)
   Song management within a playlist (add, remove, reorder, list)

 */
public class PlaylistDAO {

    // Playlist CRUD

    /**
     * Creates a new playlist for the given user.
     *
     * @return the newly created {@link Playlist} with its generated id, or
     *         {@code null} on failure.
     */
    public Playlist createPlaylist(int userId, String name, String description) {
        String sql = """
                INSERT INTO playlists (user_id, name, description)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    return getPlaylistById(newId);
                }
            }
        } catch (SQLException e) {
            System.err.println("createPlaylist error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a single playlist by its id (without preloading its songs).
     */
    public Playlist getPlaylistById(int playlistId) {
        String sql = "SELECT id, user_id, name, description, created_at FROM playlists WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPlaylist(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("getPlaylistById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all playlists that belong to a user (without preloading songs).
     */
    public List<Playlist> getPlaylistsByUser(int userId) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT id, user_id, name, description, created_at FROM playlists WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    playlists.add(mapPlaylist(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getPlaylistsByUser error: " + e.getMessage());
        }
        return playlists;
    }

    /**
     * Retrieves a playlist along with all of its songs, ordered by position.
     */
    public Playlist getPlaylistWithSongs(int playlistId) {
        Playlist playlist = getPlaylistById(playlistId);
        if (playlist == null) return null;

        List<Song> songs = getSongsForPlaylist(playlistId);
        playlist.setSongs(songs);
        return playlist;
    }

    /**
     * Updates the name and/or description of an existing playlist.
     *
     * @return {@code true} if the row was updated.
     */
    public boolean updatePlaylist(int playlistId, String newName, String newDescription) {
        String sql = "UPDATE playlists SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newDescription);
            ps.setInt(3, playlistId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updatePlaylist error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a playlist and all of its song entries (cascade).
     *
     * @return {@code true} if the playlist was deleted.
     */
    public boolean deletePlaylist(int playlistId) {
        // Remove songs first to respect FK constraints (SQLite may not enforce them
        // unless PRAGMA foreign_keys = ON, but explicit removal is safer).
        removeSongsFromPlaylist(playlistId);

        String sql = "DELETE FROM playlists WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deletePlaylist error: " + e.getMessage());
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Song management inside a playlist
    // -------------------------------------------------------------------------

    /**
     * Adds a song to the end of a playlist.
     *
     * @return {@code true} on success.
     */
    public boolean addSongToPlaylist(int playlistId, int songId) {
        int nextPosition = getNextPosition(playlistId);
        String sql = "INSERT OR IGNORE INTO playlist_songs (playlist_id, song_id, position) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            ps.setInt(3, nextPosition);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addSongToPlaylist error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Removes a song from a playlist and compacts the remaining positions.
     *
     * @return {@code true} if the song was removed.
     */
    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            boolean removed = ps.executeUpdate() > 0;
            if (removed) {
                compactPositions(playlistId);
            }
            return removed;
        } catch (SQLException e) {
            System.err.println("removeSongFromPlaylist error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Moves a song to a new position within the playlist, shifting other songs
     * accordingly.
     *
     * @param playlistId the playlist to modify
     * @param songId     the song to reposition
     * @param newPosition the target 1-based position
     * @return {@code true} on success
     */
    public boolean moveSong(int playlistId, int songId, int newPosition) {
        try {
            Connection conn = connection();
            conn.setAutoCommit(false);
            try {
                // Find the current position of the song
                int currentPosition;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT position FROM playlist_songs WHERE playlist_id = ? AND song_id = ?")) {
                    ps.setInt(1, playlistId);
                    ps.setInt(2, songId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        currentPosition = rs.getInt("position");
                    }
                }

                if (currentPosition == newPosition) {
                    conn.commit();
                    return true;
                }

                // Shift the songs in between
                if (newPosition < currentPosition) {
                    // Moving up: push affected songs down by 1
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE playlist_songs SET position = position + 1 " +
                            "WHERE playlist_id = ? AND position >= ? AND position < ?")) {
                        ps.setInt(1, playlistId);
                        ps.setInt(2, newPosition);
                        ps.setInt(3, currentPosition);
                        ps.executeUpdate();
                    }
                } else {
                    // Moving down: pull affected songs up by 1
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE playlist_songs SET position = position - 1 " +
                            "WHERE playlist_id = ? AND position > ? AND position <= ?")) {
                        ps.setInt(1, playlistId);
                        ps.setInt(2, currentPosition);
                        ps.setInt(3, newPosition);
                        ps.executeUpdate();
                    }
                }

                // Place the song at its new position
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE playlist_songs SET position = ? WHERE playlist_id = ? AND song_id = ?")) {
                    ps.setInt(1, newPosition);
                    ps.setInt(2, playlistId);
                    ps.setInt(3, songId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("moveSong error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns all songs in a playlist ordered by their position.
     */
    public List<Song> getSongsForPlaylist(int playlistId) {
        List<Song> songs = new ArrayList<>();
        String sql = """
                SELECT s.id, s.title, s.artist, s.album, s.duration_seconds,
                       s.genre, s.release_year, s.added_at
                FROM songs s
                JOIN playlist_songs ps ON s.id = ps.song_id
                WHERE ps.playlist_id = ?
                ORDER BY ps.position ASC
                """;
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapSong(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getSongsForPlaylist error: " + e.getMessage());
        }
        return songs;
    }

    /**
     * Returns the full {@link PlaylistSong} entries for a playlist (includes
     * position and addedAt metadata).
     */
    public List<PlaylistSong> getPlaylistSongEntries(int playlistId) {
        List<PlaylistSong> entries = new ArrayList<>();
        String sql = """
                SELECT s.id, s.title, s.artist, s.album, s.duration_seconds,
                       s.genre, s.release_year, s.added_at AS song_added_at,
                       ps.position, ps.added_at AS ps_added_at
                FROM songs s
                JOIN playlist_songs ps ON s.id = ps.song_id
                WHERE ps.playlist_id = ?
                ORDER BY ps.position ASC
                """;
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Song song = new Song(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("artist"),
                            rs.getString("album"),
                            rs.getInt("duration_seconds"),
                            rs.getString("genre"),
                            rs.getInt("release_year"),
                            rs.getString("song_added_at")  // use the alias, not 'added_at'
                    );
                    int position   = rs.getInt("position");
                    String addedAt = rs.getString("ps_added_at");
                    entries.add(new PlaylistSong(playlistId, song, position, addedAt));
                }
            }
        } catch (SQLException e) {
            System.err.println("getPlaylistSongEntries error: " + e.getMessage());
        }
        return entries;
    }

    /**
     * Returns {@code true} if the given song is already in the playlist.
     */
    public boolean containsSong(int playlistId, int songId) {
        String sql = "SELECT 1 FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("containsSong error: " + e.getMessage());
        }
        return false;
    }

    // Private helpers

    /** Removes ALL songs from a playlist (used before deleting the playlist). */
    private void removeSongsFromPlaylist(int playlistId) {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("removeSongsFromPlaylist error: " + e.getMessage());
        }
    }

    /** Returns the next available position (max + 1) for a playlist. */
    private int getNextPosition(int playlistId) {
        String sql = "SELECT COALESCE(MAX(position), 0) + 1 FROM playlist_songs WHERE playlist_id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("getNextPosition error: " + e.getMessage());
        }
        return 1;
    }

    /**
     * Re-numbers positions as 1, 2, 3, … after a removal, so there are no gaps.
     */
    private void compactPositions(int playlistId) {
        List<Integer> songIds = new ArrayList<>();
        String selectSql = "SELECT song_id FROM playlist_songs WHERE playlist_id = ? ORDER BY position ASC";
        try (PreparedStatement ps = connection().prepareStatement(selectSql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) songIds.add(rs.getInt("song_id"));
            }
        } catch (SQLException e) {
            System.err.println("compactPositions (select) error: " + e.getMessage());
            return;
        }

        String updateSql = "UPDATE playlist_songs SET position = ? WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement ps = connection().prepareStatement(updateSql)) {
            for (int i = 0; i < songIds.size(); i++) {
                ps.setInt(1, i + 1);
                ps.setInt(2, playlistId);
                ps.setInt(3, songIds.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("compactPositions (update) error: " + e.getMessage());
        }
    }

    private Playlist mapPlaylist(ResultSet rs) throws SQLException {
        return new Playlist(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("created_at")
        );
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

    private Connection connection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }
}
