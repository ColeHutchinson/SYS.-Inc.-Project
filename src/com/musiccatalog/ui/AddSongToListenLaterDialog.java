package com.musiccatalog.ui;

import com.musiccatalog.dao.ListenLaterDAO;
import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.dao.SongDAO.SortField;
import com.musiccatalog.dao.SongDAO.SortOrder;
import com.musiccatalog.model.Song;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for picking a song from the catalog to add to the Listen Later list.
 * Songs already in the list are shown grayed out and cannot be selected.
 */
public class AddSongToListenLaterDialog extends JDialog {

    private final ListenLaterDAO listenLaterDAO;
    private final SongDAO songDAO = new SongDAO();
    private final int userId;
    private boolean added = false;

    private JTextField searchField;
    private JTable songTable;
    private AllSongsTableModel tableModel;
    private JButton addBtn;

    public AddSongToListenLaterDialog(Frame parent, ListenLaterDAO listenLaterDAO, int userId) {
        super(parent, "Add Song to Listen Later", true);
        this.listenLaterDAO = listenLaterDAO;
        this.userId = userId;
        buildUI();
        loadSongs("");
        setSize(650, 450);
        setResizable(true);
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(22);
        searchPanel.add(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadSongs(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { loadSongs(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { loadSongs(searchField.getText()); }
        });
        root.add(searchPanel, BorderLayout.NORTH);

        tableModel = new AllSongsTableModel();
        songTable = new JTable(tableModel);
        songTable.setRowHeight(24);
        songTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songTable.setShowGrid(false);
        songTable.setIntercellSpacing(new Dimension(0, 0));
        songTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        songTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        songTable.getTableHeader().setBackground(new Color(220, 225, 240));

        songTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                boolean alreadyIn = tableModel.isAlreadyInList(row);
                if (isSelected) {
                    setBackground(new Color(180, 210, 255));
                    setForeground(alreadyIn ? Color.GRAY : Color.BLACK);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 248, 252));
                    setForeground(alreadyIn ? Color.GRAY : Color.BLACK);
                }
                return this;
            }
        });

        int[] colWidths = {200, 150, 160, 65};
        for (int i = 0; i < colWidths.length; i++) {
            songTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        songTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = songTable.getSelectedRow();
                addBtn.setEnabled(row >= 0 && !tableModel.isAlreadyInList(row));
            }
        });

        root.add(new JScrollPane(songTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        addBtn = new JButton("Add to Listen Later");
        addBtn.setBackground(new Color(40, 160, 80));
        addBtn.setForeground(Color.BLACK);
        addBtn.setEnabled(false);
        addBtn.addActionListener(e -> doAdd());

        btnPanel.add(cancelBtn);
        btnPanel.add(addBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadSongs(String filter) {
        List<Song> all = songDAO.findAll(SortField.TITLE, SortOrder.ASC);
        String lower = filter.trim().toLowerCase();
        List<Song> filtered = new ArrayList<>();
        for (Song s : all) {
            if (lower.isEmpty()
                    || s.getTitle().toLowerCase().contains(lower)
                    || s.getArtist().toLowerCase().contains(lower)) {
                filtered.add(s);
            }
        }
        tableModel.setSongs(filtered, userId, listenLaterDAO);
        addBtn.setEnabled(false);
    }

    private void doAdd() {
        int row = songTable.getSelectedRow();
        if (row < 0 || tableModel.isAlreadyInList(row)) return;
        Song song = tableModel.getSongAt(row);
        listenLaterDAO.addSongToListenLater(userId, song.getId());
        added = true;
        tableModel.markAdded(row);
        addBtn.setEnabled(false);
    }

    public boolean wasAdded() {
        return added;
    }

    private static class AllSongsTableModel extends AbstractTableModel {
        private static final String[] COLS = {"Title", "Artist", "Album", "Duration"};
        private List<Song> songs = new ArrayList<>();
        private boolean[] inList = new boolean[0];

        void setSongs(List<Song> songs, int userId, ListenLaterDAO dao) {
            this.songs = songs;
            this.inList = new boolean[songs.size()];
            for (int i = 0; i < songs.size(); i++) {
                inList[i] = dao.isSongInListenLater(userId, songs.get(i).getId());
            }
            fireTableDataChanged();
        }

        void markAdded(int row) {
            if (row >= 0 && row < inList.length) {
                inList[row] = true;
                fireTableRowsUpdated(row, row);
            }
        }

        boolean isAlreadyInList(int row) {
            return row >= 0 && row < inList.length && inList[row];
        }

        Song getSongAt(int row) { return songs.get(row); }

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
                default -> "";
            };
        }
    }
}