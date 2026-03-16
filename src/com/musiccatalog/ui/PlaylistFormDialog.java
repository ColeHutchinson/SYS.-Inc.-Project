package com.musiccatalog.ui;

import com.musiccatalog.dao.PlaylistDAO;
import com.musiccatalog.model.Playlist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for creating a new playlist or editing an existing one.
 * Pass null for playlistToEdit to open in create mode.
 */
public class PlaylistFormDialog extends JDialog {

    private final PlaylistDAO playlistDAO;
    private final int userId;
    private final Playlist playlistToEdit;
    private Playlist result = null;

    private JTextField nameField;
    private JTextArea descriptionArea;

    /** Create mode */
    public PlaylistFormDialog(Frame parent, PlaylistDAO playlistDAO, int userId) {
        this(parent, playlistDAO, userId, null);
    }

    /** Edit mode — pass the existing playlist to prefill fields */
    public PlaylistFormDialog(Frame parent, PlaylistDAO playlistDAO, int userId, Playlist playlistToEdit) {
        super(parent, playlistToEdit == null ? "New Playlist" : "Edit Playlist", true);
        this.playlistDAO = playlistDAO;
        this.userId = userId;
        this.playlistToEdit = playlistToEdit;
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(20, 25, 15, 25));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Name *"), gbc);
        nameField = new JTextField(25);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description"), gbc);
        descriptionArea = new JTextArea(3, 25);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        form.add(descScroll, gbc);

        root.add(form, BorderLayout.CENTER);

        // Prefill if editing
        if (playlistToEdit != null) {
            nameField.setText(playlistToEdit.getName());
            if (playlistToEdit.getDescription() != null) {
                descriptionArea.setText(playlistToEdit.getDescription());
            }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(playlistToEdit == null ? "Create" : "Save Changes");
        saveBtn.setBackground(new Color(50, 120, 200));
        saveBtn.setForeground(Color.BLACK);
        saveBtn.addActionListener(e -> doSave());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void doSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Playlist name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String description = descriptionArea.getText().trim();
        String descOrNull = description.isEmpty() ? null : description;

        if (playlistToEdit == null) {
            result = playlistDAO.createPlaylist(userId, name, descOrNull);
        } else {
            boolean updated = playlistDAO.updatePlaylist(playlistToEdit.getId(), name, descOrNull);
            if (updated) {
                result = playlistDAO.getPlaylistById(playlistToEdit.getId());
            }
        }
        dispose();
    }

    public Playlist getResult() {
        return result;
    }
}
