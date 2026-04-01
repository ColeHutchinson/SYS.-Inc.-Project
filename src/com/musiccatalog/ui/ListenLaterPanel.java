package com.musiccatalog.ui;

import com.musiccatalog.dao.ListenLaterDAO;
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
 * Panel displaying the user's Listen Later song list with a table view.
 */
public class ListenLaterPanel extends JPanel {

    private final User currentUser;
    private final ListenLaterDAO listenLaterDAO;

    private ListenLaterTableModel tableModel;
    private JTable songsTable;
    private JLabel headerLabel;
    private JButton removeSongBtn;
    private JButton clearAllBtn;

    public ListenLaterPanel(User currentUser, ListenLaterDAO listenLaterDAO) {
        super(new BorderLayout());
        this.currentUser = currentUser;
        this.listenLaterDAO = listenLaterDAO;
        buildUI();
    }

    private void buildUI() {
        headerLabel = new JLabel("Listen Later");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        headerLabel.setBorder(new EmptyBorder(12, 14, 8, 14));
        add(headerLabel, BorderLayout.NORTH);

        tableModel = new ListenLaterTableModel();
        songsTable = new JTable(tableModel);
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

        int[] colWidths = {200, 150, 160, 65, 100};
        for (int i = 0; i < colWidths.length; i++) {
            songsTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        songsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateButtonStates();
        });

        add(new JScrollPane(songsTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 213, 225)));

        JButton addSongBtn = new JButton("+ Add Song");
        addSongBtn.setBackground(new Color(50, 120, 200));
        addSongBtn.setForeground(Color.BLACK);
        addSongBtn.addActionListener(e -> onAddSong());

        removeSongBtn = new JButton("Remove");
        removeSongBtn.setBackground(new Color(200, 60, 60));
        removeSongBtn.setForeground(Color.BLACK);
        removeSongBtn.setEnabled(false);
        removeSongBtn.addActionListener(e -> onRemoveSong());

        clearAllBtn = new JButton("Clear All");
        clearAllBtn.setBackground(new Color(200, 60, 60));
        clearAllBtn.setForeground(Color.BLACK);
        clearAllBtn.setEnabled(false);
        clearAllBtn.addActionListener(e -> onClearAll());

        btnPanel.add(addSongBtn);
        btnPanel.add(removeSongBtn);
        btnPanel.add(clearAllBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /** Reloads songs from the database. Call when switching to this view. */
    public void refresh() {
        List<Song> songs = listenLaterDAO.getListenLaterSongs(currentUser.getId());
        tableModel.setSongs(songs);
        int count = songs.size();
        headerLabel.setText("Listen Later (" + count + " song" + (count == 1 ? "" : "s") + ")");
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean rowSelected = songsTable.getSelectedRow() >= 0;
        removeSongBtn.setEnabled(rowSelected);
        clearAllBtn.setEnabled(tableModel.getRowCount() > 0);
    }

    private void onAddSong() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        AddSongToListenLaterDialog dialog = new AddSongToListenLaterDialog(
                parentFrame, listenLaterDAO, currentUser.getId());
        dialog.setVisible(true);
        if (dialog.wasAdded()) {
            refresh();
        }
    }

    private void onRemoveSong() {
        int row = songsTable.getSelectedRow();
        if (row < 0) return;
        Song song = tableModel.getSongAt(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove \"" + song.getTitle() + "\" from Listen Later?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            listenLaterDAO.removeSongFromListenLater(currentUser.getId(), song.getId());
            refresh();
        }
    }

    private void onClearAll() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Clear all songs from your Listen Later list?",
                "Confirm Clear All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            for (Song s : tableModel.getAllSongs()) {
                listenLaterDAO.removeSongFromListenLater(currentUser.getId(), s.getId());
            }
            refresh();
        }
    }

    private static class ListenLaterTableModel extends AbstractTableModel {
        private static final String[] COLS = {"Title", "Artist", "Album", "Duration", "Date Added"};
        private List<Song> songs = new ArrayList<>();

        void setSongs(List<Song> songs) {
            this.songs = new ArrayList<>(songs);
            fireTableDataChanged();
        }

        Song getSongAt(int row) { return songs.get(row); }
        List<Song> getAllSongs() { return new ArrayList<>(songs); }

        @Override public int getRowCount() { return songs.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int col) { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Song s = songs.get(row);
            return switch (col) {
                case 0 -> s.getTitle();
                case 1 -> s.getArtist();
                case 2 -> s.getAlbum() != null ? s.getAlbum() : "";
                case 3 -> s.getFormattedDuration();
                case 4 -> s.getAddedAt() != null && s.getAddedAt().length() >= 10
                        ? s.getAddedAt().substring(0, 10) : "";
                default -> "";
            };
        }
    }
}