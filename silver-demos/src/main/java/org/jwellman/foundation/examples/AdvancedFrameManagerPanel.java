package org.jwellman.foundation.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jwellman.foundation.interfaces.uiContext;
import org.jwellman.foundation.model.FrameDescriptor;

/**
 * Advanced frame manager panel demonstrating Silver tier desktop management features.
 * <p>
 * This panel provides a comprehensive frame management UI with:
 * <ul>
 * <li>Visual list of all registered frames (except the control panel)</li>
 * <li>Per-frame visibility toggle (show/hide)</li>
 * <li>Per-frame attach/detach to external window</li>
 * <li>Editable frame titles via context menu or double-click</li>
 * <li>Visual status indicator (green=visible, grey=hidden)</li>
 * <li>Event-driven updates - automatically refreshes when frames change</li>
 * </ul>
 * <p>
 * Design:
 * <ul>
 * <li>Each frame is represented by a FrameEntryPanel with 20px status indicator</li>
 * <li>Entry panels are stacked vertically in a scrollable container</li>
 * <li>Right-click or double-click title to edit</li>
 * <li>Buttons provide quick access to common operations</li>
 * <li>Automatically updates via lifecycle listeners (no manual refresh needed)</li>
 * </ul>
 * <p>
 * This demonstrates how to build rich desktop management UIs on top of
 * Foundation's FrameDescriptor registry and desktop manager.
 *
 * @author Foundation Framework
 */
public class AdvancedFrameManagerPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final uiContext context;
    private final JPanel entriesContainer;

    /**
     * Creates a new advanced frame manager panel.
     *
     * @param context The UI context to manage frames for
     */
    public AdvancedFrameManagerPanel(uiContext context) {
        super(new BorderLayout());
        this.context = context;

        // Header
        JLabel headerLabel = new JLabel("Advanced Frame Manager");
        headerLabel.setFont(headerLabel.getFont().deriveFont(16f));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(headerLabel, BorderLayout.NORTH);

        // Container for frame entries (vertical stack)
        entriesContainer = new JPanel();
        entriesContainer.setLayout(new BoxLayout(entriesContainer, BoxLayout.Y_AXIS));
        entriesContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Make it scrollable
        JScrollPane scrollPane = new JScrollPane(entriesContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Initial population
        refresh();
    }

    /**
     * Refreshes the frame list from the registry.
     */
    public void refresh() {
        // Use SwingUtilities.invokeLater to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            entriesContainer.removeAll();

            List<FrameDescriptor> allFrames = context.getRegistrations();

            // Filter out the control panel
            List<FrameDescriptor> filteredFrames = new java.util.ArrayList<>();
            for (FrameDescriptor descriptor : allFrames) {
                // Skip the control panel - it's permanent and shouldn't be managed
                if (!"control".equals(descriptor.getPanelId())) {
                    filteredFrames.add(descriptor);
                }
            }

            if (filteredFrames.isEmpty()) {
                JLabel emptyLabel = new JLabel("No frames registered");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                entriesContainer.add(emptyLabel);
            } else {
                for (FrameDescriptor descriptor : filteredFrames) {
                    FrameEntryPanel entryPanel = new FrameEntryPanel(descriptor, context, this);
                    entriesContainer.add(entryPanel);
                    entriesContainer.add(Box.createVerticalStrut(5)); // Spacing
                }
            }

            entriesContainer.revalidate();
            entriesContainer.repaint();
        });
    }

    /**
     * Individual frame entry panel with controls.
     */
    private static class FrameEntryPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private final FrameDescriptor descriptor;
        private final uiContext context;
        private final AdvancedFrameManagerPanel parentPanel;
        private final JLabel titleLabel;
        private final JButton visibilityButton;
        private final JButton attachDetachButton;
        private final JPanel statusIndicator;

        // Colors for status indicator
        private static final Color VISIBLE_COLOR = new Color(144, 238, 144);    // Light green
        private static final Color HIDDEN_COLOR = new Color(105, 105, 105);     // Dark grey

        public FrameEntryPanel(FrameDescriptor descriptor, uiContext context, AdvancedFrameManagerPanel parentPanel) {
            super(new BorderLayout());
            this.descriptor = descriptor;
            this.context = context;
            this.parentPanel = parentPanel;

            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            // Create status indicator (left side, 20px wide)
            statusIndicator = new JPanel();
            statusIndicator.setPreferredSize(new Dimension(20, 0));
            add(statusIndicator, BorderLayout.WEST);

            // Create content panel with GridBagLayout for the rest
            JPanel contentPanel = new JPanel(new GridBagLayout());
            contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Column 1: Frame ID and Title (expandable)
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.gridwidth = 1;

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            // Frame ID
            JLabel idLabel = new JLabel(descriptor.getFullId());
            idLabel.setFont(idLabel.getFont().deriveFont(11f));
            idLabel.setForeground(Color.DARK_GRAY);
            infoPanel.add(idLabel);

            // Frame Title (editable)
            String title = descriptor.getWindowTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "(No title)";
            }
            titleLabel = new JLabel(title);
            titleLabel.setFont(titleLabel.getFont().deriveFont(13f));
            titleLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            titleLabel.setToolTipText("Right-click or double-click to edit title");
            infoPanel.add(titleLabel);

            // Add edit functionality to title
            addTitleEditListeners();

            contentPanel.add(infoPanel, gbc);

            // Column 2: Visibility Toggle Button
            gbc.gridx = 1;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;

            visibilityButton = new JButton(descriptor.isVisible() ? "Hide" : "Show");
            visibilityButton.setPreferredSize(new Dimension(70, 25));
            visibilityButton.addActionListener(this::toggleVisibility);
            contentPanel.add(visibilityButton, gbc);

            // Column 3: Attach/Detach Button
            gbc.gridx = 2;

            attachDetachButton = new JButton(descriptor.isDetached() ? "Attach" : "Detach");
            attachDetachButton.setPreferredSize(new Dimension(80, 25));
            attachDetachButton.addActionListener(this::toggleAttachDetach);
            contentPanel.add(attachDetachButton, gbc);

            add(contentPanel, BorderLayout.CENTER);

            updateButtonStates();
        }

        /**
         * Adds mouse listeners for title editing (right-click menu + double-click).
         */
        private void addTitleEditListeners() {
            // Create popup menu
            JPopupMenu popup = new JPopupMenu();
            JMenuItem editItem = new JMenuItem("Edit Title...");
            editItem.addActionListener(e -> showEditTitleDialog());
            popup.add(editItem);

            // Right-click shows popup
            titleLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        popup.show(titleLabel, e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Double-click also opens edit dialog
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        showEditTitleDialog();
                    }
                }
            });
        }

        /**
         * Shows a dialog to edit the frame title.
         */
        private void showEditTitleDialog() {
            String currentTitle = descriptor.getWindowTitle();
            if (currentTitle == null) {
                currentTitle = "";
            }

            String newTitle = JOptionPane.showInputDialog(
                this,
                "Enter new title for frame:",
                currentTitle
            );

            if (newTitle != null && !newTitle.equals(currentTitle)) {
                descriptor.setWindowTitle(newTitle);
                titleLabel.setText(newTitle.isEmpty() ? "(No title)" : newTitle);

                // Update the actual window title if visible
                if (descriptor.getInternalFrame() != null) {
                    descriptor.getInternalFrame().setTitle(newTitle);
                }
                if (descriptor.getExternalFrame() != null) {
                    descriptor.getExternalFrame().setTitle(newTitle);
                }
            }
        }

        /**
         * Toggles frame visibility.
         */
        private void toggleVisibility(ActionEvent e) {
            if (descriptor.isVisible()) {
                descriptor.hide();
            } else {
                descriptor.show();
            }
            updateButtonStates();
        }

        /**
         * Toggles attach/detach state.
         */
        private void toggleAttachDetach(ActionEvent e) {
            // Use context.detachPanel() which toggles between attached/detached
            context.detachPanel(descriptor.getPanelId());
            updateButtonStates();

            // Trigger a full refresh since detach doesn't fire lifecycle events
            // This ensures all entry states are current
            parentPanel.refresh();
        }

        /**
         * Updates button labels and states based on current frame state.
         */
        private void updateButtonStates() {
            visibilityButton.setText(descriptor.isVisible() ? "Hide" : "Show");
            attachDetachButton.setText(descriptor.isDetached() ? "Attach" : "Detach");

            // Update status indicator color based on visibility
            if (descriptor.isVisible()) {
                statusIndicator.setBackground(VISIBLE_COLOR);  // Light green
            } else {
                statusIndicator.setBackground(HIDDEN_COLOR);   // Dark grey
            }
        }
    }
}
