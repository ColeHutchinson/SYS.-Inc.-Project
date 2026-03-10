package com.musiccatalog.ui;

import com.musiccatalog.dao.SongDAO;
import com.musiccatalog.dao.SongDAO.SortField;
import com.musiccatalog.dao.SongDAO.SortOrder;
import com.musiccatalog.model.Song;
import com.musiccatalog.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main application window showing the song catalog.
 */
public class CatalogWindow extends JFrame {

    private final User currentUser;
    private final SongDAO songDAO = new SongDAO();

    private SongTableModel tableModel;
    private JTable songTable;
    private TableRowSorter<SongTableModel> rowSorter;

    private JTextField searchField;
    private JComboBox<String> genreFilter;
    private JComboBox<String> sortFieldCombo;
    private JComboBox<String> sortOrderCombo;
    private JLabel statusLabel;
    private JLabel userLabel;

    public CatalogWindow(User user) {
        this.currentUser = user;
        setTitle("Music Catalog");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 650));
        buildUI();
        loadSongs();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildToolbar(), BorderLayout.CENTER);  // holds toolbar + table together
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 46));
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("🎵  Music Catalog");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        userLabel = new JLabel("Logged in as: " + currentUser.getUsername());
        userLabel.setForeground(new Color(180, 180, 200));
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(new Color(200, 60, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(userLabel);
        rightPanel.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildToolbar() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // === Toolbar ===
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(new Color(240, 242, 248));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 213, 225)));

        // Search
        toolbar.add(new JLabel("Search:"));
        searchField = new JTextField(18);
        searchField.setToolTipText("Search by title, artist, album, or genre");
        toolbar.add(searchField);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Genre filter
        toolbar.add(new JLabel("Genre:"));
        genreFilter = new JComboBox<>();
        genreFilter.setPreferredSize(new Dimension(130, 26));
        toolbar.add(genreFilter);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Sort field
        toolbar.add(new JLabel("Sort by:"));
        sortFieldCombo = new JComboBox<>(new String[]{
            "Title", "Artist", "Album", "Duration", "Genre", "Year", "Date Added"
        });
        toolbar.add(sortFieldCombo);

        // Sort order
        sortOrderCombo = new JComboBox<>(new String[]{"↑ Ascending", "↓ Descending"});
        toolbar.add(sortOrderCombo);

        JButton applyBtn = new JButton("Apply");
        applyBtn.setBackground(new Color(50, 120, 200));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.addActionListener(e -> loadSongs());
        toolbar.add(applyBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            genreFilter.setSelectedIndex(0);
            loadSongs();
        });
        toolbar.add(clearBtn);

        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Add song button
        JButton addBtn = new JButton("+ Add Song");
        addBtn.setBackground(new Color(40, 160, 80));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> openAddSongDialog());
        toolbar.add(addBtn);

        // === Table ===
        tableModel = new SongTableModel();
        songTable = new JTable(tableModel);
        songTable.setRowHeight(26);
        songTable.setShowGrid(false);
        songTable.setIntercellSpacing(new Dimension(0, 0));
        songTable.setSelectionBackground(new Color(180, 210, 255));
        songTable.setSelectionForeground(Color.BLACK);
        songTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        songTable.getTableHeader().setBackground(new Color(220, 225, 240));
        songTable.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Alternating row color
        songTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 248, 252));
                }
                return this;
            }
        });

        // Column widths
        int[] colWidths = {40, 200, 150, 160, 65, 100, 55};
        for (int i = 0; i < colWidths.length; i++) {
            songTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        // Double-click to edit
        songTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditSongDialog();
                }
            }
        });

        // Right-click context menu
        JPopupMenu contextMenu = buildContextMenu();
        songTable.setComponentPopupMenu(contextMenu);

        // Live search as user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadSongs(); }
            public void removeUpdate(DocumentEvent e) { loadSongs(); }
            public void changedUpdate(DocumentEvent e) { loadSongs(); }
        });

        genreFilter.addActionListener(e -> loadSongs());
        sortFieldCombo.addActionListener(e -> loadSongs());
        sortOrderCombo.addActionListener(e -> loadSongs());

        JScrollPane scrollPane = new JScrollPane(songTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPopupMenu buildContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("✏️  Edit Song");
        editItem.addActionListener(e -> openEditSongDialog());

        JMenuItem deleteItem = new JMenuItem("🗑️  Delete Song");
        deleteItem.setForeground(new Color(180, 30, 30));
        deleteItem.addActionListener(e -> deleteSelectedSong());

        menu.add(editItem);
        menu.addSeparator();
        menu.add(deleteItem);
        return menu;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(4, 12, 4, 12));
        bar.setBackground(new Color(240, 242, 248));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 213, 225)),
            new EmptyBorder(4, 12, 4, 12)
        ));

        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);

        JLabel hintLabel = new JLabel("Double-click to edit  •  Right-click for options");
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hintLabel.setForeground(new Color(160, 160, 180));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(hintLabel, BorderLayout.EAST);
        return bar;
    }

    private void loadSongs() {
        // Refresh genre filter
        String selectedGenre = (String) genreFilter.getSelectedItem();
        genreFilter.removeAllItems();
        List<String> genres = songDAO.findAllGenres();
        for (String g : genres) genreFilter.addItem(g);
        if (selectedGenre != null) genreFilter.setSelectedItem(selectedGenre);

        SortField sortField = getSortField();
        SortOrder sortOrder = getSortOrder();
        String searchText = searchField.getText().trim();
        String genreSelected = (String) genreFilter.getSelectedItem();

        List<Song> songs;

        if (!searchText.isEmpty()) {
            songs = songDAO.search(searchText, sortField, sortOrder);
        } else if (genreSelected != null && !genreSelected.equals("All Genres")) {
            songs = songDAO.findByGenre(genreSelected, sortField, sortOrder);
        } else {
            songs = songDAO.findAll(sortField, sortOrder);
        }

        tableModel.setSongs(songs);
        updateStatus(songs.size());
    }

    private SortField getSortField() {
        return switch (sortFieldCombo.getSelectedIndex()) {
            case 1 -> SortField.ARTIST;
            case 2 -> SortField.ALBUM;
            case 3 -> SortField.DURATION;
            case 4 -> SortField.GENRE;
            case 5 -> SortField.YEAR;
            case 6 -> SortField.DATE_ADDED;
            default -> SortField.TITLE;
        };
    }

    private SortOrder getSortOrder() {
        return sortOrderCombo.getSelectedIndex() == 1 ? SortOrder.DESC : SortOrder.ASC;
    }

    private void updateStatus(int count) {
        int total = songDAO.countAll();
        if (count == total) {
            statusLabel.setText(total + " songs in catalog");
        } else {
            statusLabel.setText("Showing " + count + " of " + total + " songs");
        }
    }

    private void openAddSongDialog() {
        SongFormDialog dialog = new SongFormDialog(this, null);
        dialog.setVisible(true);
        Song result = dialog.getResult();
        if (result != null) {
            songDAO.addSong(result);
            loadSongs();
            JOptionPane.showMessageDialog(this, "Song added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openEditSongDialog() {
        int selectedRow = songTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a song to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Song song = tableModel.getSongAt(selectedRow);
        SongFormDialog dialog = new SongFormDialog(this, song);
        dialog.setVisible(true);
        Song result = dialog.getResult();
        if (result != null) {
            songDAO.updateSong(result);
            loadSongs();
        }
    }

    private void deleteSelectedSong() {
        int selectedRow = songTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a song to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Song song = tableModel.getSongAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + song.getTitle() + "\" by " + song.getArtist() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            songDAO.deleteSong(song.getId());
            loadSongs();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);
                User user = loginDialog.getAuthenticatedUser();
                if (user != null) {
                    new CatalogWindow(user).setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        }
    }
}
