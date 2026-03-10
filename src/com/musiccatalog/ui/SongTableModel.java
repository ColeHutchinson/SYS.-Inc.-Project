package com.musiccatalog.ui;

import com.musiccatalog.model.Song;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel that backs the song catalog JTable.
 */
public class SongTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
        "#", "Title", "Artist", "Album", "Duration", "Genre", "Year"
    };

    private List<Song> songs = new ArrayList<>();

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        fireTableDataChanged();
    }

    public Song getSongAt(int row) {
        return songs.get(row);
    }

    @Override
    public int getRowCount() {
        return songs.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMNS[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Song s = songs.get(row);
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> s.getTitle();
            case 2 -> s.getArtist();
            case 3 -> s.getAlbum() != null ? s.getAlbum() : "—";
            case 4 -> s.getFormattedDuration();
            case 5 -> s.getGenre() != null ? s.getGenre() : "—";
            case 6 -> s.getReleaseYear() > 0 ? s.getReleaseYear() : "—";
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 0) return Integer.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
