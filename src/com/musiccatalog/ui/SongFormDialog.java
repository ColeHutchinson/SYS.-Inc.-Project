package com.musiccatalog.ui;

import com.musiccatalog.model.Song;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for adding or editing a song.
 */
public class SongFormDialog extends JDialog {

    private final boolean isEdit;
    private Song result = null;

    private JTextField titleField, artistField, albumField, genreField, yearField;
    private JTextField minutesField, secondsField;

    public SongFormDialog(Frame parent, Song songToEdit) {
        super(parent, songToEdit == null ? "Add Song" : "Edit Song", true);
        this.isEdit = (songToEdit != null);
        buildUI(songToEdit);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void buildUI(Song song) {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(20, 25, 15, 25));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Title
        addRow(form, gbc, row++, "Title *", titleField = new JTextField(25));
        addRow(form, gbc, row++, "Artist *", artistField = new JTextField(25));
        addRow(form, gbc, row++, "Album", albumField = new JTextField(25));
        addRow(form, gbc, row++, "Genre", genreField = new JTextField(25));
        addRow(form, gbc, row++, "Year", yearField = new JTextField(10));

        // Duration row
        JPanel durPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        minutesField = new JTextField(4);
        secondsField = new JTextField(4);
        durPanel.add(minutesField);
        durPanel.add(new JLabel("min"));
        durPanel.add(secondsField);
        durPanel.add(new JLabel("sec"));
        addRow(form, gbc, row++, "Duration *", durPanel);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(isEdit ? "Save Changes" : "Add Song");
        saveBtn.setBackground(new Color(50, 120, 200));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> doSave(song));

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        // Prefill if editing
        if (song != null) {
            titleField.setText(song.getTitle());
            artistField.setText(song.getArtist());
            albumField.setText(song.getAlbum() != null ? song.getAlbum() : "");
            genreField.setText(song.getGenre() != null ? song.getGenre() : "");
            yearField.setText(song.getReleaseYear() > 0 ? String.valueOf(song.getReleaseYear()) : "");
            minutesField.setText(String.valueOf(song.getDurationSeconds() / 60));
            secondsField.setText(String.valueOf(song.getDurationSeconds() % 60));
        }

        setContentPane(root);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void doSave(Song original) {
        String title = titleField.getText().trim();
        String artist = artistField.getText().trim();
        String album = albumField.getText().trim();
        String genre = genreField.getText().trim();
        String yearStr = yearField.getText().trim();
        String minsStr = minutesField.getText().trim();
        String secsStr = secondsField.getText().trim();

        if (title.isEmpty() || artist.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Artist are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int durationSeconds;
        try {
            int mins = minsStr.isEmpty() ? 0 : Integer.parseInt(minsStr);
            int secs = secsStr.isEmpty() ? 0 : Integer.parseInt(secsStr);
            durationSeconds = mins * 60 + secs;
            if (durationSeconds <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid duration.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int year = 0;
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid year.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        result = new Song(
            original != null ? original.getId() : 0,
            title, artist,
            album.isEmpty() ? null : album,
            durationSeconds,
            genre.isEmpty() ? null : genre,
            year, null
        );
        dispose();
    }

    public Song getResult() {
        return result;
    }
}
