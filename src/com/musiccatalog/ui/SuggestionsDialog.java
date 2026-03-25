package com.musiccatalog.ui;

import com.musiccatalog.dao.SongSuggestionDAO;
import com.musiccatalog.model.SongSuggestion;
import com.musiccatalog.model.User;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dialog for admins to view and manage song suggestions.
 */
public class SuggestionsDialog extends JDialog {

    private final SongSuggestionDAO suggestionDAO = new SongSuggestionDAO();
    private SuggestionsTableModel tableModel;
    private JTable suggestionsTable;

    public SuggestionsDialog(Frame parent, User user) {
        super(parent, "Song Suggestions", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(parent);

        buildUI();
        loadSuggestions();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Song Suggestions");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        root.add(titleLabel, BorderLayout.NORTH);

        // Table
        tableModel = new SuggestionsTableModel();
        suggestionsTable = new JTable(tableModel);
        suggestionsTable.setRowHeight(25);
        suggestionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(suggestionsTable);
        root.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSuggestions());

        JButton deleteBtn = new JButton("Delete Suggestion");
        deleteBtn.setForeground(new Color(180, 30, 30));
        deleteBtn.addActionListener(e -> deleteSelectedSuggestion());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(closeBtn);

        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadSuggestions() {
        List<SongSuggestion> suggestions = suggestionDAO.findAll(SongSuggestionDAO.Status.PENDING);
        tableModel.setSuggestions(suggestions);
    }

    private void deleteSelectedSuggestion() {
        int selectedRow = suggestionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a suggestion to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SongSuggestion suggestion = tableModel.getSuggestionAt(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete suggestion \"" + suggestion.getTitle() + "\" by " + suggestion.getArtist() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (suggestionDAO.deleteSuggestion(suggestion.getId())) {
                loadSuggestions();
                JOptionPane.showMessageDialog(this, "Suggestion deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete suggestion.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class SuggestionsTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Title", "Artist", "Suggested By", "Suggested At"};
        private List<SongSuggestion> suggestions;

        public void setSuggestions(List<SongSuggestion> suggestions) {
            this.suggestions = suggestions;
            fireTableDataChanged();
        }

        public SongSuggestion getSuggestionAt(int row) {
            return suggestions.get(row);
        }

        @Override
        public int getRowCount() {
            return suggestions != null ? suggestions.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SongSuggestion suggestion = suggestions.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> suggestion.getTitle();
                case 1 -> suggestion.getArtist();
                case 2 -> "User " + suggestion.getSuggestedBy(); // In real app, join with users table
                case 3 -> suggestion.getSuggestedAt();
                default -> null;
            };
        }

    }
}
