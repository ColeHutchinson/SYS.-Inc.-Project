package com.musiccatalog.ui;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.model.Playlist;
import com.musiccatalog.model.PlaylistSong;
import com.musiccatalog.model.Song;
import com.musiccatalog.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel displaying the user's playlists with a split view:
 * left = playlist list, right = songs in the selected playlist.
 */
public class PlaylistMenuPanel extends JPanel {

    private final User currentUser;
    private final PlaylistDAO playlistDAO;

    // Left panel
    private DefaultListModel<Playlist> listModel;
    private JList<Playlist> playlistList;
    private JButton editPlaylistBtn;
    private JButton deletePlaylistBtn;

    // Right panel
    private PlaylistSongsTableModel songsTableModel;
    private JTable songsTable;
    private JLabel songsPanelLabel;
    private JButton addSongBtn;
    private JButton removeSongBtn;
    private JButton moveUpBtn;
    private JButton moveDownBtn;

    public PlaylistMenuPanel(User currentUser, PlaylistDAO playlistDAO) {
        super(new BorderLayout());
        this.currentUser = currentUser;
        this.playlistDAO = playlistDAO;
        buildUI();
    }

    private void buildUI() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        splitPane.setDividerLocation(260);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    // --- Left panel: playlist list ---

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(210, 213, 225)));

        JLabel title = new JLabel("My Playlists");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(12, 12, 8, 12));
        panel.add(title, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        playlistList = new JList<>(listModel);
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        playlistList.setFixedCellHeight(56);
        playlistList.setCellRenderer(new PlaylistCellRenderer());
        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onPlaylistSelected();
        });
        panel.add(new JScrollPane(playlistList), BorderLayout.CENTER);

        // New Playlist on top row, Edit + Delete on bottom row
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        btnPanel.setBorder(new EmptyBorder(8, 10, 10, 10));

        JButton newPlaylistBtn = new JButton("+ New Playlist");
        newPlaylistBtn.setBackground(new Color(40, 160, 80));
        newPlaylistBtn.setForeground(Color.BLACK);
        newPlaylistBtn.addActionListener(e -> onNewPlaylist());

        JPanel editDeleteRow = new JPanel(new GridLayout(1, 2, 6, 0));
        editPlaylistBtn = new JButton("Edit");
        editPlaylistBtn.setEnabled(false);
        editPlaylistBtn.addActionListener(e -> onEditPlaylist());

        deletePlaylistBtn = new JButton("Delete");
        deletePlaylistBtn.setBackground(new Color(200, 60, 60));
        deletePlaylistBtn.setForeground(Color.BLACK);
        deletePlaylistBtn.setEnabled(false);
        deletePlaylistBtn.addActionListener(e -> onDeletePlaylist());

        editDeleteRow.add(editPlaylistBtn);
        editDeleteRow.add(deletePlaylistBtn);

        btnPanel.add(newPlaylistBtn);
        btnPanel.add(editDeleteRow);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Right panel: songs in selected playlist ---

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        songsPanelLabel = new JLabel("Select a playlist");
        songsPanelLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        songsPanelLabel.setBorder(new EmptyBorder(12, 14, 8, 14));
        panel.add(songsPanelLabel, BorderLayout.NORTH);

        songsTableModel = new PlaylistSongsTableModel();
        songsTable = new JTable(songsTableModel);
        songsTable.setRowHeight(26);
        songsTable.setShowGrid(false);
        songsTable.setIntercellSpacing(new Dimension(0, 0));
        songsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        songsTable.setSelectionBackground(new Color(180, 210, 255));
        songsTable.setSelectionForeground(Color.BLACK);
        songsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        songsTable.getTableHeader().setBackground(new Color(220, 225, 240));

        songsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 248, 252));
                }
                return this;
            }
        });

        int[] colWidths = {40, 180, 130, 130, 60, 100};
        for (int i = 0; i < colWidths.length; i++) {
            songsTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        songsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateSongButtonStates();
        });

        panel.add(new JScrollPane(songsTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 213, 225)));

        addSongBtn = new JButton("+ Add Song");
        addSongBtn.setBackground(new Color(50, 120, 200));
        addSongBtn.setForeground(Color.BLACK);
        addSongBtn.setEnabled(false);
        addSongBtn.addActionListener(e -> onAddSong());

        removeSongBtn = new JButton("Remove");
        removeSongBtn.setBackground(new Color(200, 60, 60));
        removeSongBtn.setForeground(Color.BLACK);
        removeSongBtn.setEnabled(false);
        removeSongBtn.addActionListener(e -> onRemoveSong());

        moveUpBtn = new JButton("Move Up");
        moveUpBtn.setEnabled(false);
        moveUpBtn.addActionListener(e -> onMoveSong(-1));

        moveDownBtn = new JButton("Move Down");
        moveDownBtn.setEnabled(false);
        moveDownBtn.addActionListener(e -> onMoveSong(1));

        btnPanel.add(addSongBtn);
        btnPanel.add(removeSongBtn);
        btnPanel.add(moveUpBtn);
        btnPanel.add(moveDownBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Public API ---

    /** Reloads the playlist list from the database. Call when switching to this view. */
    public void refresh() {
        Playlist selected = playlistList.getSelectedValue();
        listModel.clear();
        List<Playlist> playlists = playlistDAO.getPlaylistsByUser(currentUser.getId());
        for (Playlist p : playlists) listModel.addElement(p);

        // Restore prior selection if it still exists
        if (selected != null) {
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).getId() == selected.getId()) {
                    playlistList.setSelectedIndex(i);
                    return;
                }
            }
        }
        clearSongsPanel();
    }

    // --- Interactions ---

    private void onPlaylistSelected() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected == null) {
            clearSongsPanel();
            return;
        }

        editPlaylistBtn.setEnabled(true);
        deletePlaylistBtn.setEnabled(true);
        addSongBtn.setEnabled(true);

        // getPlaylistWithSongs populates songs list so getFormattedTotalDuration() works
        Playlist full = playlistDAO.getPlaylistWithSongs(selected.getId());
        if (full != null) {
            songsPanelLabel.setText(full.getName() + " - " + full.getFormattedTotalDuration());
        } else {
            songsPanelLabel.setText(selected.getName());
        }

        // getPlaylistSongEntries gives position + addedAt metadata per song
        songsTableModel.setEntries(playlistDAO.getPlaylistSongEntries(selected.getId()));
        updateSongButtonStates();
    }

    private void clearSongsPanel() {
        songsTableModel.setEntries(new ArrayList<>());
        songsPanelLabel.setText("Select a playlist");
        editPlaylistBtn.setEnabled(false);
        deletePlaylistBtn.setEnabled(false);
        addSongBtn.setEnabled(false);
        removeSongBtn.setEnabled(false);
        moveUpBtn.setEnabled(false);
        moveDownBtn.setEnabled(false);
    }

    private void updateSongButtonStates() {
        int row = songsTable.getSelectedRow();
        int total = songsTableModel.getRowCount();
        boolean rowSelected = row >= 0;
        removeSongBtn.setEnabled(rowSelected);
        moveUpBtn.setEnabled(rowSelected && row > 0);
        moveDownBtn.setEnabled(rowSelected && row < total - 1);
    }

    private void onNewPlaylist() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        PlaylistFormDialog dialog = new PlaylistFormDialog(parentFrame, playlistDAO, currentUser.getId());
        dialog.setVisible(true);
        Playlist created = dialog.getResult();
        if (created != null) {
            refresh();
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).getId() == created.getId()) {
                    playlistList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void onEditPlaylist() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected == null) return;
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        PlaylistFormDialog dialog = new PlaylistFormDialog(parentFrame, playlistDAO, currentUser.getId(), selected);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            refresh();
        }
    }

    private void onDeletePlaylist() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete playlist \"" + selected.getName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            playlistDAO.deletePlaylist(selected.getId());
            refresh();
        }
    }

    private void onAddSong() {
        Playlist selected = playlistList.getSelectedValue();
        if (selected == null) return;
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        AddSongToPlaylistDialog dialog = new AddSongToPlaylistDialog(parentFrame, playlistDAO, selected.getId());
        dialog.setVisible(true);
        if (dialog.wasAdded()) {
            onPlaylistSelected();
        }
    }

    private void onRemoveSong() {
        Playlist selected = playlistList.getSelectedValue();
        int row = songsTable.getSelectedRow();
        if (selected == null || row < 0) return;
        PlaylistSong entry = songsTableModel.getEntryAt(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove \"" + entry.getSong().getTitle() + "\" from this playlist?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            playlistDAO.removeSongFromPlaylist(selected.getId(), entry.getSong().getId());
            onPlaylistSelected();
        }
    }

    private void onMoveSong(int direction) {
        Playlist selected = playlistList.getSelectedValue();
        int row = songsTable.getSelectedRow();
        if (selected == null || row < 0) return;
        PlaylistSong entry = songsTableModel.getEntryAt(row);
        int newPosition = entry.getPosition() + direction;
        playlistDAO.moveSong(selected.getId(), entry.getSong().getId(), newPosition);
        onPlaylistSelected();
        // Re-select the moved song at its new row position
        int newRow = row + direction;
        if (newRow >= 0 && newRow < songsTableModel.getRowCount()) {
            songsTable.setRowSelectionInterval(newRow, newRow);
        }
    }

    // --- Playlist cell renderer: shows name + description ---

    private static class PlaylistCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Playlist p) {
                String desc = p.getDescription();
                if (desc != null && !desc.isBlank()) {
                    setText("<html><b>" + p.getName() + "</b><br>"
                            + "<span style='color:gray;font-size:10px'>" + desc + "</span></html>");
                } else {
                    setText("<html><b>" + p.getName() + "</b></html>");
                }
            }
            setBorder(new EmptyBorder(6, 12, 6, 12));
            return this;
        }
    }

    // --- Songs table model using PlaylistSong entries ---

    private static class PlaylistSongsTableModel extends AbstractTableModel {
        private static final String[] COLS = {"#", "Title", "Artist", "Album", "Duration", "Date Added"};
        private List<PlaylistSong> entries = new ArrayList<>();

        void setEntries(List<PlaylistSong> entries) {
            this.entries = entries;
            fireTableDataChanged();
        }

        PlaylistSong getEntryAt(int row) { return entries.get(row); }

        @Override public int getRowCount() { return entries.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int col) { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            PlaylistSong ps = entries.get(row);
            Song s = ps.getSong();
            return switch (col) {
                case 0 -> ps.getPosition();
                case 1 -> s.getTitle();
                case 2 -> s.getArtist();
                case 3 -> s.getAlbum() != null ? s.getAlbum() : "";
                case 4 -> s.getFormattedDuration();
                case 5 -> ps.getAddedAt() != null && ps.getAddedAt().length() >= 10
                        ? ps.getAddedAt().substring(0, 10) : "";
                default -> "";
            };
        }
    }
}
