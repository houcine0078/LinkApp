package Interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class CustomFilePicker extends JDialog {
    private File currentDir;
    private JList<File> fileList;
    private DefaultListModel<File> listModel;
    private JTextField pathField;
    private File selectedFile = null;
    private JLabel previewLabel;

    public CustomFilePicker(Frame parent, String title, File startDir) {
        super(parent, title, true);
        setSize(600, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        currentDir = (startDir != null && startDir.isDirectory()) ? startDir : new File(System.getProperty("user.home"));

        pathField = new JTextField(currentDir.getAbsolutePath());
        pathField.setEditable(false);
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pathField.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        mainPanel.add(pathField, BorderLayout.SOUTH);

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setCellRenderer(new FileCellRenderer());
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(320, 260));

        // Image preview panel
        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewLabel.setVerticalAlignment(JLabel.CENTER);
        previewLabel.setPreferredSize(new Dimension(180, 180));
        previewLabel.setBorder(BorderFactory.createTitledBorder("Preview"));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(previewLabel, BorderLayout.EAST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton openButton = new JButton("Open");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(openButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.PAGE_END);

        add(mainPanel, BorderLayout.CENTER);

        openButton.addActionListener(e -> {
            File f = fileList.getSelectedValue();
            if (f != null && f.isFile()) {
                selectedFile = f;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            selectedFile = null;
            dispose();
        });

        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    File f = fileList.getSelectedValue();
                    if (f != null && f.isDirectory()) {
                        setDirectory(f);
                    }
                }
            }
        });

        fileList.addListSelectionListener(e -> updatePreview());

        setDirectory(currentDir);
    }

    private void setDirectory(File dir) {
        currentDir = dir;
        pathField.setText(currentDir.getAbsolutePath());
        File[] files = currentDir.listFiles();
        listModel.clear();
        if (currentDir.getParentFile() != null) {
            listModel.addElement(currentDir.getParentFile()); // ".." for up
        }
        if (files != null) {
            for (File f : files) {
                // Only show directories and .png files, skip hidden files (except parent dir)
                if (!f.isHidden() && (f.isDirectory() || f.getName().toLowerCase().endsWith(".png"))) {
                    listModel.addElement(f);
                }
            }
        }
        previewLabel.setIcon(null);
        previewLabel.setText("");
    }

    private void updatePreview() {
        File f = fileList.getSelectedValue();
        if (f != null && f.isFile() && f.getName().toLowerCase().endsWith(".png")) {
            try {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                previewLabel.setIcon(new ImageIcon(img));
                previewLabel.setText("");
            } catch (Exception ex) {
                previewLabel.setIcon(null);
                previewLabel.setText("Cannot preview");
            }
        } else {
            previewLabel.setIcon(null);
            if (f != null && f.isDirectory()) {
                previewLabel.setText("<html><center>Folder<br>Double-click to open</center></html>");
            } else if (f != null && f.getParentFile() != null && f.getParentFile().equals(f)) {
                previewLabel.setText("[..]");
            } else {
                previewLabel.setText("");
            }
        }
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    private static class FileCellRenderer extends DefaultListCellRenderer {
        private final Icon folderIcon = UIManager.getIcon("FileView.directoryIcon");
        private final Icon fileIcon = UIManager.getIcon("FileView.fileIcon");
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            File file = (File) value;
            String name = file.getName();
            if (file.isDirectory()) {
                if (file.getParentFile() != null && file.getParentFile().equals(file)) {
                    name = "[..]";
                } else if (name.isEmpty()) {
                    name = file.getAbsolutePath();
                } else {
                    name = "[DIR] " + name;
                }
                setIcon(folderIcon);
            } else {
                setIcon(fileIcon);
            }
            Component c = super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            return c;
        }
    }
}
