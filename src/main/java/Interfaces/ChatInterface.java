package Interfaces;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import UI.RoundedBorder;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import services.FirebaseAuthService;
import java.io.File;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridBagLayout;
// Add import for JFileDialog
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import java.awt.image.BufferedImage;
import UI.UserListRenderer;
import UI.ModernChatListRenderer;
import model.Message;
import model.UserInfo;
import model.GroupInfo;
import model.ChatItem;


public class ChatInterface extends JFrame {
    private JPanel chatPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JButton emojiButton;
    private JButton fileButton;
    private JList<ChatItem> chatList;
    private DefaultListModel<ChatItem> chatListModel;
    private String currentUserEmail;
    private ChatItem currentChat;
    private JLabel chattingWithLabel;
    private JLabel onlineStatusLabel;
    private JScrollPane chatScrollPane;
    private ScheduledExecutorService scheduler;
    private String lastLoadedChatId = null;
    private JTextField searchField;
    private Map<String, UserInfo> userCache = new HashMap<>();
    private Map<String, GroupInfo> groupCache = new HashMap<>();

    // Theme colors (same as login)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color BORDER_COLOR = new Color(189, 195, 199);
    private static final Color MESSAGE_SENT = PRIMARY_COLOR;
    private static final Color MESSAGE_RECEIVED = new Color(229, 229, 234);
    private static final Color ONLINE_GREEN = new Color(52, 199, 89);
    private static final Color GROUP_COLOR = new Color(155, 89, 182);

    public ChatInterface(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
        setupLookAndFeel();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        fetchUsersAndGroups();
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("List.selectionBackground", PRIMARY_COLOR);
            UIManager.put("List.selectionForeground", Color.WHITE);
            UIManager.put("ScrollBar.width", 8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        setTitle("LinkApp - " + currentUserEmail.split("@")[0]);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        getContentPane().setBackground(BACKGROUND_COLOR);
        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatPanel = new JPanel();
        inputField = new JTextField();
        sendButton = new JButton();
        emojiButton = new JButton();
        fileButton = new JButton();
        searchField = new JTextField();
        chattingWithLabel = new JLabel();
        onlineStatusLabel = new JLabel();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.0);
        splitPane.setBorder(null);
        splitPane.setDividerSize(1);
        splitPane.getLeftComponent().setMinimumSize(new Dimension(300, 0));
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        JPanel header = createLeftHeader();
        JPanel searchPanel = createSearchPanel();
        JScrollPane chatScrollPane = createChatList();
        leftPanel.add(header, BorderLayout.NORTH);
        leftPanel.add(searchPanel, BorderLayout.CENTER);
        leftPanel.add(chatScrollPane, BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createLeftHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 20, 15, 20));
        JLabel titleLabel = new JLabel("Messages");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton profileButton = new JButton(FontIcon.of(FontAwesome.USER, 20, PRIMARY_COLOR));
        profileButton.setBorderPainted(false);
        profileButton.setContentAreaFilled(false);
        profileButton.setFocusPainted(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileButton.setToolTipText("Profile");
        profileButton.addActionListener(e -> showProfileDialog());

        JButton newGroupButton = new JButton(FontIcon.of(FontAwesome.USERS, 20, GROUP_COLOR));
        newGroupButton.setBorderPainted(false);
        newGroupButton.setContentAreaFilled(false);
        newGroupButton.setFocusPainted(false);
        newGroupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newGroupButton.setToolTipText("New Group");
        newGroupButton.addActionListener(e -> showCreateGroupDialog());

        JButton logoutButton = new JButton(FontIcon.of(FontAwesome.SIGN_OUT, 20, ACCENT_COLOR));
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setToolTipText("Logout");
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(profileButton);
        buttonPanel.add(newGroupButton);
        buttonPanel.add(logoutButton);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createSearchPanel() {
        // Outer panel with fixed padding and background
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // padding around

        // Inner wrapper with rounded background
        JPanel searchWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        searchWrapper.setOpaque(false);
        searchWrapper.setMaximumSize(new Dimension(300, 40)); // fix width & height
        searchWrapper.setPreferredSize(new Dimension(400, 40));
        searchWrapper.setMinimumSize(new Dimension(400, 40));
        searchWrapper.setLayout(new BorderLayout());

        // The actual text field
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(BACKGROUND_COLOR);
        searchField.setBorder(new EmptyBorder(12, 16, 12, 16));

        searchWrapper.add(searchField, BorderLayout.CENTER);
        searchPanel.add(Box.createHorizontalGlue()); // center horizontally
        searchPanel.add(searchWrapper);
        searchPanel.add(Box.createHorizontalGlue()); // center horizontally

        return searchPanel;
    }

    private JScrollPane createChatList() {
        chatList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        chatList.setFixedCellHeight(72);
        chatList.setCellRenderer(new ModernChatListRenderer());
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setBackground(Color.WHITE);
        chatList.setBorder(null);
        JScrollPane scrollPane = new JScrollPane(chatList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BACKGROUND_COLOR);
        JPanel chatHeader = createChatHeader();
        JPanel chatArea = createChatArea();
        JPanel inputArea = createInputArea();
        rightPanel.add(chatHeader, BorderLayout.NORTH);
        rightPanel.add(chatArea, BorderLayout.CENTER);
        rightPanel.add(inputArea, BorderLayout.SOUTH);
        return rightPanel;
    }

    private JPanel createChatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(15, 25, 15, 25)
        ));
        JPanel userInfo = new JPanel(new BorderLayout());
        userInfo.setBackground(Color.WHITE);
        chattingWithLabel = new JLabel("Select a conversation");
        chattingWithLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chattingWithLabel.setForeground(TEXT_COLOR);
        onlineStatusLabel = new JLabel();
        onlineStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        onlineStatusLabel.setForeground(TEXT_COLOR);
        userInfo.add(chattingWithLabel, BorderLayout.NORTH);
        userInfo.add(onlineStatusLabel, BorderLayout.SOUTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);
        JButton groupInfo = createHeaderButton(FontIcon.of(FontAwesome.INFO_CIRCLE, 20, PRIMARY_COLOR));
        groupInfo.addActionListener(e -> showGroupInfo());
        JButton videoCall = createHeaderButton(FontIcon.of(FontAwesome.VIDEO_CAMERA, 20, PRIMARY_COLOR));
        JButton voiceCall = createHeaderButton(FontIcon.of(FontAwesome.PHONE, 20, PRIMARY_COLOR));
        JButton moreOptions = createHeaderButton(FontIcon.of(FontAwesome.ELLIPSIS_H, 20, PRIMARY_COLOR));
        actions.add(groupInfo);
        actions.add(videoCall);
        actions.add(voiceCall);
        actions.add(moreOptions);
        header.add(userInfo, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JButton createHeaderButton(FontIcon icon) {
        JButton button = new JButton(icon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(36, 36));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BACKGROUND_COLOR);
                button.setContentAreaFilled(true);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
            }
        });
        return button;
    }

    private JPanel createChatArea() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BACKGROUND_COLOR);
        chatPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.setBackground(BACKGROUND_COLOR);
        chatScrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND_COLOR);
        wrapper.add(chatScrollPane);
        return wrapper;
    }

    private JPanel createInputArea() {
        JPanel inputArea = new JPanel(new BorderLayout());
        inputArea.setBackground(Color.WHITE);
        inputArea.setBorder(new EmptyBorder(15, 25, 20, 25));
        JPanel inputContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                g2.dispose();
            }
        };
        inputContainer.setPreferredSize(new Dimension(0, 50));
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        inputField.setBorder(new EmptyBorder(12, 20, 12, 15));
        inputField.setBackground(BACKGROUND_COLOR);
        inputField.putClientProperty("JTextField.placeholderText", "Type a message...");
        fileButton = new JButton(FontIcon.of(FontAwesome.PAPERCLIP, 20, PRIMARY_COLOR));
        fileButton.setBorderPainted(false);
        fileButton.setContentAreaFilled(false);
        fileButton.setFocusPainted(false);
        fileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fileButton.setPreferredSize(new Dimension(40, 40));
        fileButton.addActionListener(e -> sendFileToCurrentChat());
        emojiButton = new JButton(FontIcon.of(FontAwesome.SMILE_O, 20, PRIMARY_COLOR));
        emojiButton.setBorderPainted(false);
        emojiButton.setContentAreaFilled(false);
        emojiButton.setFocusPainted(false);
        emojiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emojiButton.setPreferredSize(new Dimension(40, 40));
        emojiButton.addActionListener(e -> showEmojiPicker());
        sendButton = new JButton(FontIcon.of(FontAwesome.SEND, 20, Color.WHITE)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_COLOR.brighter());
                } else {
                    g2.setColor(PRIMARY_COLOR);
                }
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sendButton.setPreferredSize(new Dimension(42, 42));
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        buttonContainer.setBackground(BACKGROUND_COLOR);
        buttonContainer.add(fileButton);
        buttonContainer.add(emojiButton);
        buttonContainer.add(sendButton);
        inputContainer.add(inputField, BorderLayout.CENTER);
        inputContainer.add(buttonContainer, BorderLayout.EAST);
        inputArea.add(inputContainer);
        return inputArea;
    }

    private void setupEventListeners() {
        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentChat = chatList.getSelectedValue();
                if (currentChat != null) {
                    if (currentChat.isGroup) {
                        GroupInfo group = (GroupInfo) currentChat.chatInfo;
                        chattingWithLabel.setText(group.name);
                        onlineStatusLabel.setText(group.members.size() + " members");
                        onlineStatusLabel.setForeground(TEXT_COLOR);
                    } else {
                        UserInfo user = (UserInfo) currentChat.chatInfo;
                        chattingWithLabel.setText(user.name);
                        onlineStatusLabel.setText(user.status.equals("online") ? "● Online" : "Last seen recently");
                        onlineStatusLabel.setForeground(user.status.equals("online") ? ONLINE_GREEN : TEXT_COLOR);
                    }
                    clearChatPanel();
                    startMessageListener();
                } else {
                    chattingWithLabel.setText("Select a conversation");
                    onlineStatusLabel.setText("");
                    stopMessageListener();
                }
            }
        });
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterChats(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterChats(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterChats(); }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopMessageListener();
                System.exit(0);
            }
        });
    }

    private void filterChats() {
        String query = searchField.getText().toLowerCase().trim();
        chatListModel.clear();

        // Add filtered users
        for (UserInfo user : userCache.values()) {
            if (query.isEmpty() || user.name.toLowerCase().contains(query) || user.email.toLowerCase().contains(query)) {
                chatListModel.addElement(new ChatItem(user, false));
            }
        }

        // Add filtered groups
        for (GroupInfo group : groupCache.values()) {
            if (query.isEmpty() || group.name.toLowerCase().contains(query)) {
                chatListModel.addElement(new ChatItem(group, true));
            }
        }
    }

    private void fetchUsersAndGroups() {
        SwingUtilities.invokeLater(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String databaseUrl = FirebaseAuthService.getDatabaseUrl();

                // Fetch users
                String usersUrl = databaseUrl + "/users.json";
                Request usersRequest = new Request.Builder().url(usersUrl).get().build();
                Response usersResponse = client.newCall(usersRequest).execute();
                String usersResponseBody = usersResponse.body().string();
                JSONObject usersJson = new JSONObject(usersResponseBody);

                userCache.clear();
                for (String key : usersJson.keySet()) {
                    JSONObject userObj = usersJson.getJSONObject(key);
                    String email = userObj.optString("email", "");
                    if (!email.equals(currentUserEmail)) {
                        UserInfo userInfo = new UserInfo(
                                email,
                                userObj.optString("displayName", email.split("@")[0]),
                                userObj.optString("status", "offline"),
                                userObj.optString("avatar", ""),
                                userObj.optLong("lastSeen", System.currentTimeMillis())
                        );
                        userCache.put(email, userInfo);
                    }
                }

                // Fetch groups
                String groupsUrl = databaseUrl + "/groups.json";
                Request groupsRequest = new Request.Builder().url(groupsUrl).get().build();
                Response groupsResponse = client.newCall(groupsRequest).execute();
                String groupsResponseBody = groupsResponse.body().string();

                groupCache.clear();
                if (!groupsResponseBody.equals("null")) {
                    JSONObject groupsJson = new JSONObject(groupsResponseBody);
                    for (String groupId : groupsJson.keySet()) {
                        JSONObject groupObj = groupsJson.getJSONObject(groupId);
                        JSONArray membersArray = groupObj.optJSONArray("members");
                        List<String> members = new ArrayList<>();
                        if (membersArray != null) {
                            for (int i = 0; i < membersArray.length(); i++) {
                                members.add(membersArray.getString(i));
                            }
                        }

                        // Only show groups where current user is a member
                        if (members.contains(currentUserEmail)) {
                            GroupInfo groupInfo = new GroupInfo(
                                    groupId,
                                    groupObj.optString("name", ""),
                                    groupObj.optString("description", ""),
                                    members,
                                    groupObj.optString("createdBy", ""),
                                    groupObj.optLong("createdAt", System.currentTimeMillis())
                            );
                            groupCache.put(groupId, groupInfo);
                        }
                    }
                }

                // Update UI
                refreshChatList();

            } catch (IOException | JSONException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void refreshChatList() {
        chatListModel.clear();

        // Add users
        for (UserInfo user : userCache.values()) {
            chatListModel.addElement(new ChatItem(user, false));
        }

        // Add groups
        for (GroupInfo group : groupCache.values()) {
            chatListModel.addElement(new ChatItem(group, true));
        }
    }

    private void showCreateGroupDialog() {
        JDialog dialog = new JDialog(this, "Create New Group", true);
        dialog.setSize(420, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel(new BorderLayout(16, 16));
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Group name input
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setOpaque(false);
        JLabel nameLabel = new JLabel("Group Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(TEXT_COLOR);
        namePanel.add(nameLabel, BorderLayout.NORTH);
        JTextField groupNameField = new JTextField();
        groupNameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        namePanel.add(groupNameField, BorderLayout.CENTER);

        // Group description input
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setOpaque(false);
        JLabel descLabel = new JLabel("Description (optional):");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        descLabel.setForeground(TEXT_COLOR);
        descPanel.add(descLabel, BorderLayout.NORTH);
        JTextField descField = new JTextField();
        descField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descPanel.add(descField, BorderLayout.CENTER);

        // Member selection
        JPanel memberPanel = new JPanel(new BorderLayout(5, 5));
        memberPanel.setOpaque(false);
        JLabel memberLabel = new JLabel("Select Members:");
        memberLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        memberLabel.setForeground(TEXT_COLOR);
        memberPanel.add(memberLabel, BorderLayout.NORTH);

        DefaultListModel<UserInfo> memberListModel = new DefaultListModel<>();
        for (UserInfo user : userCache.values()) {
            memberListModel.addElement(user);
        }

        JList<UserInfo> memberList = new JList<>(memberListModel);
        memberList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        memberList.setCellRenderer(new UserListRenderer());
        memberList.setBackground(Color.WHITE);
        memberList.setFixedCellHeight(64);
        memberList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        memberList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = memberList.locationToIndex(e.getPoint());
                if (index != -1) {
                    if (memberList.isSelectedIndex(index)) {
                        memberList.removeSelectionInterval(index, index);
                    } else {
                        memberList.addSelectionInterval(index, index);
                    }
                }
            }
        });
        JScrollPane memberScrollPane = new JScrollPane(memberList);
        memberScrollPane.setPreferredSize(new Dimension(350, 200));
        memberScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        memberPanel.add(memberScrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);
        JButton cancelButton = new JButton("Cancel");
        JButton createButton = new JButton("Create Group");

        // Style buttons
        styleDialogButton(cancelButton, Color.WHITE, BORDER_COLOR, TEXT_COLOR);
        styleDialogButton(createButton, PRIMARY_COLOR, PRIMARY_COLOR, Color.WHITE);

        cancelButton.addActionListener(e -> dialog.dispose());
        createButton.addActionListener(e -> {
            String groupName = groupNameField.getText().trim();
            if (groupName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a group name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<UserInfo> selectedUsers = memberList.getSelectedValuesList();
            if (selectedUsers.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select at least one member.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            createGroup(groupName, descField.getText().trim(), selectedUsers);
            dialog.dispose();
        });
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        topPanel.setOpaque(false);
        topPanel.add(namePanel);
        topPanel.add(descPanel);

        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(memberPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Rounded border for dialog
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(24, 24, 24, 24)));

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    // Helper to style dialog buttons
    private void styleDialogButton(JButton button, Color bg, Color border, Color fg) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(18, border),
                BorderFactory.createLineBorder(border, 1)));
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setEnabled(true);
        button.repaint();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.repaint();
            }
        });
        // Custom paint for blue gradient
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (bg.equals(PRIMARY_COLOR)) {
                    GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR.brighter(), 0, c.getHeight(), PRIMARY_COLOR.darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);
                } else {
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);
                }
                super.paint(g2, c);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private void createGroup(String name, String description, List<UserInfo> members) {
        try {
            OkHttpClient client = new OkHttpClient();
            String databaseUrl = FirebaseAuthService.getDatabaseUrl();
            String groupId = "group_" + System.currentTimeMillis();
            String url = databaseUrl + "/groups/" + groupId + ".json";

            JSONObject groupObj = new JSONObject();
            groupObj.put("name", name);
            groupObj.put("description", description);
            groupObj.put("createdBy", currentUserEmail);
            groupObj.put("createdAt", System.currentTimeMillis());

            JSONArray membersArray = new JSONArray();
            membersArray.put(currentUserEmail); // Add creator
            for (UserInfo user : members) {
                membersArray.put(user.email);
            }
            groupObj.put("members", membersArray);

            RequestBody body = RequestBody.create(groupObj.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).put(body).build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // Refresh the groups list
                fetchUsersAndGroups();
                JOptionPane.showMessageDialog(this, "Group created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create group.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showGroupInfo() {
        if (currentChat == null || !currentChat.isGroup) return;

        GroupInfo group = (GroupInfo) currentChat.chatInfo;
        JDialog dialog = new JDialog(this, "Group Info - " + group.name, true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Group details
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        detailsPanel.add(new JLabel("Name: " + group.name));
        detailsPanel.add(new JLabel("Description: " + (group.description.isEmpty() ? "No description" : group.description)));
        detailsPanel.add(new JLabel("Created: " + new SimpleDateFormat("MMM d, yyyy").format(new Date(group.createdAt))));

        // Members list
        JPanel memberPanel = new JPanel(new BorderLayout(5, 5));
        memberPanel.add(new JLabel("Members (" + group.members.size() + "):"), BorderLayout.NORTH);

        DefaultListModel<String> memberListModel = new DefaultListModel<>();
        for (String memberEmail : group.members) {
            UserInfo user = userCache.get(memberEmail);
            String displayName = user != null ? user.name + " (" + memberEmail + ")" : memberEmail;
            if (memberEmail.equals(group.createdBy)) {
                displayName += " (Admin)";
            }
            memberListModel.addElement(displayName);
        }

        JList<String> memberList = new JList<>(memberListModel);
        JScrollPane memberScrollPane = new JScrollPane(memberList);
        memberScrollPane.setPreferredSize(new Dimension(350, 200));
        memberPanel.add(memberScrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addMemberButton = new JButton("Add Member");
        JButton leaveGroupButton = new JButton("Leave Group");
        JButton closeButton = new JButton("Close");

        addMemberButton.addActionListener(e -> showAddMemberDialog(group));
        leaveGroupButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to leave this group?",
                    "Leave Group",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                leaveGroup(group);
                dialog.dispose();
            }
        });
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addMemberButton);
        buttonPanel.add(leaveGroupButton);
        buttonPanel.add(closeButton);

        contentPanel.add(detailsPanel, BorderLayout.NORTH);
        contentPanel.add(memberPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private void showAddMemberDialog(GroupInfo group) {
        JDialog dialog = new JDialog(this, "Add Members to " + group.name, true);
        dialog.setSize(350, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Available users (not already in group)
        DefaultListModel<UserInfo> availableUsersModel = new DefaultListModel<>();
        for (UserInfo user : userCache.values()) {
            if (!group.members.contains(user.email)) {
                availableUsersModel.addElement(user);
            }
        }

        if (availableUsersModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All users are already members of this group.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<UserInfo> availableUsersList = new JList<>(availableUsersModel);
        availableUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availableUsersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                UserInfo user = (UserInfo) value;
                setText(user.name + " (" + user.email + ")");
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(availableUsersList);
        scrollPane.setPreferredSize(new Dimension(300, 250));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Add Members");

        cancelButton.addActionListener(e -> dialog.dispose());
        addButton.addActionListener(e -> {
            List<UserInfo> selectedUsers = availableUsersList.getSelectedValuesList();
            if (selectedUsers.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select at least one user.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            addMembersToGroup(group, selectedUsers);
            dialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        contentPanel.add(new JLabel("Select users to add:"), BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private void addMembersToGroup(GroupInfo group, List<UserInfo> newMembers) {
        try {
            OkHttpClient client = new OkHttpClient();
            String databaseUrl = FirebaseAuthService.getDatabaseUrl();
            String url = databaseUrl + "/groups/" + group.id + "/members.json";

            // Get current members and add new ones
            List<String> updatedMembers = new ArrayList<>(group.members);
            for (UserInfo user : newMembers) {
                if (!updatedMembers.contains(user.email)) {
                    updatedMembers.add(user.email);
                }
            }

            JSONArray membersArray = new JSONArray();
            for (String member : updatedMembers) {
                membersArray.put(member);
            }

            RequestBody body = RequestBody.create(membersArray.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).put(body).build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // Update local cache
                group.members = updatedMembers;
                groupCache.put(group.id, group);

                // Send notification message to group
                sendSystemMessage(group.id, currentUserEmail.split("@")[0] + " added " +
                        newMembers.stream().map(u -> u.name).reduce((a, b) -> a + ", " + b).orElse("") + " to the group");

                JOptionPane.showMessageDialog(this, "Members added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add members.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void leaveGroup(GroupInfo group) {
        try {
            OkHttpClient client = new OkHttpClient();
            String databaseUrl = FirebaseAuthService.getDatabaseUrl();
            String url = databaseUrl + "/groups/" + group.id + "/members.json";

            // Remove current user from members list
            List<String> updatedMembers = new ArrayList<>(group.members);
            updatedMembers.remove(currentUserEmail);

            if (updatedMembers.isEmpty()) {
                // Delete group if no members left
                String deleteUrl = databaseUrl + "/groups/" + group.id + ".json";
                Request deleteRequest = new Request.Builder().url(deleteUrl).delete().build();
                client.newCall(deleteRequest).execute();
            } else {
                // Update members list
                JSONArray membersArray = new JSONArray();
                for (String member : updatedMembers) {
                    membersArray.put(member);
                }

                RequestBody body = RequestBody.create(membersArray.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder().url(url).put(body).build();
                client.newCall(request).execute();

                // Send notification message
                sendSystemMessage(group.id, currentUserEmail.split("@")[0] + " left the group");
            }

            // Remove from local cache and refresh UI
            groupCache.remove(group.id);
            refreshChatList();

            // Clear current chat if it was this group
            if (currentChat != null && currentChat.isGroup &&
                    ((GroupInfo) currentChat.chatInfo).id.equals(group.id)) {
                currentChat = null;
                chattingWithLabel.setText("Select a conversation");
                onlineStatusLabel.setText("");
                clearChatPanel();
                stopMessageListener();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to leave group.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendSystemMessage(String groupId, String message) {
        try {
            OkHttpClient client = new OkHttpClient();
            String databaseUrl = FirebaseAuthService.getDatabaseUrl();
            long timestamp = System.currentTimeMillis();
            String url = databaseUrl + "/messages/group_" + groupId + "/" + timestamp + ".json";

            JSONObject msgObj = new JSONObject();
            msgObj.put("from", "SYSTEM");
            msgObj.put("to", "group_" + groupId);
            msgObj.put("text", message);
            msgObj.put("timestamp", timestamp);
            msgObj.put("isSystem", true);

            RequestBody body = RequestBody.create(msgObj.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).put(body).build();
            client.newCall(request).execute();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && currentChat != null) {
            try {
                OkHttpClient client = new OkHttpClient();
                String databaseUrl = FirebaseAuthService.getDatabaseUrl();
                String chatId;
                String toField;

                if (currentChat.isGroup) {
                    GroupInfo group = (GroupInfo) currentChat.chatInfo;
                    chatId = "group_" + group.id;
                    toField = chatId;
                } else {
                    UserInfo user = (UserInfo) currentChat.chatInfo;
                    chatId = getChatId(currentUserEmail, user.email);
                    toField = user.email;
                }

                long timestamp = System.currentTimeMillis();
                String url = databaseUrl + "/messages/" + chatId + "/" + timestamp + ".json";

                JSONObject msgObj = new JSONObject();
                msgObj.put("from", currentUserEmail);
                msgObj.put("to", toField);
                msgObj.put("text", message);
                msgObj.put("timestamp", timestamp);

                RequestBody body = RequestBody.create(msgObj.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder().url(url).put(body).build();
                client.newCall(request).execute();

                inputField.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void startMessageListener() {
        stopMessageListener();
        if (currentChat == null) return;

        String chatId;
        if (currentChat.isGroup) {
            GroupInfo group = (GroupInfo) currentChat.chatInfo;
            chatId = "group_" + group.id;
        } else {
            UserInfo user = (UserInfo) currentChat.chatInfo;
            chatId = getChatId(currentUserEmail, user.email);
        }

        lastLoadedChatId = chatId;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> loadMessages(chatId), 0, 1, TimeUnit.SECONDS);
    }

    private void stopMessageListener() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void loadMessages(String chatId) {
        try {
            OkHttpClient client = new OkHttpClient();
            String databaseUrl = FirebaseAuthService.getDatabaseUrl();
            String url = databaseUrl + "/messages/" + chatId + ".json";
            Request request = new Request.Builder().url(url).get().build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            JSONObject json;
            try {
                json = new JSONObject(responseBody);
            } catch (JSONException e) {
                json = new JSONObject();
            }
            List<Message> messages = new ArrayList<>();
            for (String key : json.keySet()) {
                JSONObject msgObj = json.getJSONObject(key);
                messages.add(new Message(
                        msgObj.optString("from", ""),
                        msgObj.optString("to", ""),
                        msgObj.optString("text", ""),
                        msgObj.optLong("timestamp", 0),
                        msgObj.optBoolean("isSystem", false)
                ));
            }
            messages.sort(Comparator.comparingLong(m -> m.timestamp));
            SwingUtilities.invokeLater(() -> {
                if (!chatId.equals(lastLoadedChatId)) return;
                clearChatPanel();
                String lastDate = "";
                for (Message msg : messages) {
                    String currentDate = new SimpleDateFormat("MMMM d, yyyy").format(new Date(msg.timestamp));
                    if (!currentDate.equals(lastDate)) {
                        addDateSeparator(currentDate);
                        lastDate = currentDate;
                    }
                    addModernMessageBubble(msg);
                }
                chatPanel.revalidate();
                chatPanel.repaint();
                scrollToBottom();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addDateSeparator(String date) {
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_COLOR);
        dateLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel separator = new JPanel(new FlowLayout(FlowLayout.CENTER));
        separator.setOpaque(false);
        separator.setBorder(new EmptyBorder(15, 0, 10, 0));
        separator.add(dateLabel);
        chatPanel.add(separator);
    }

    private void addModernMessageBubble(Message msg) {
        boolean isMe = msg.from.equals(currentUserEmail);
        boolean isSystem = msg.isSystem;

        if (isSystem) {
            // System message (group notifications)
            JPanel systemPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            systemPanel.setOpaque(false);
            systemPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

            JLabel systemLabel = new JLabel(msg.text);
            systemLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            systemLabel.setForeground(TEXT_COLOR);
            systemLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
            systemLabel.setOpaque(true);
            systemLabel.setBackground(new Color(240, 240, 240));

            systemPanel.add(systemLabel);
            chatPanel.add(systemPanel);
            return;
        }

        // File message rendering
        if ("file".equals(msg.type)) {
            JPanel messagePanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            messagePanel.setOpaque(false);
            messagePanel.setBorder(new EmptyBorder(4, 0, 4, 0));
            JPanel bubble = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isMe ? MESSAGE_SENT : MESSAGE_RECEIVED);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
            };
            bubble.setLayout(new BorderLayout());
            bubble.setBorder(new EmptyBorder(12, 16, 12, 16));
            JLabel fileLabel = new JLabel("<html><b>📎 " + msg.fileName + "</b> (" + readableFileSize(msg.fileSize) + ")<br><a href='" + msg.fileUrl + "'>Download</a></html>");
            fileLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            fileLabel.setForeground(isMe ? Color.WHITE : TEXT_COLOR);
            fileLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new java.net.URI(msg.fileUrl));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            bubble.add(fileLabel, BorderLayout.CENTER);
            messagePanel.add(bubble);
            chatPanel.add(messagePanel);
            return;
        }

        JPanel messagePanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        messagePanel.setOpaque(false);
        messagePanel.setBorder(new EmptyBorder(4, 0, 4, 0));
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isMe ? MESSAGE_SENT : MESSAGE_RECEIVED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(12, 16, 12, 16));

        // For group messages, show sender name if not from current user
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        if (currentChat != null && currentChat.isGroup && !isMe) {
            UserInfo sender = userCache.get(msg.from);
            String senderName = sender != null ? sender.name : msg.from.split("@")[0];
            JLabel senderLabel = new JLabel(senderName);
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            senderLabel.setForeground(GROUP_COLOR);
            senderLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
            contentPanel.add(senderLabel, BorderLayout.NORTH);
        }

        JLabel textLabel = new JLabel("<html><div style='max-width: 280px; word-wrap: break-word;'>" +
                msg.text.replace("\n", "<br>") + "</div></html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textLabel.setForeground(isMe ? Color.WHITE : TEXT_COLOR);

        JLabel timeLabel = new JLabel(new SimpleDateFormat("h:mm a").format(new Date(msg.timestamp)));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(isMe ? new Color(255, 255, 255, 180) : TEXT_COLOR);
        timeLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        contentPanel.add(textLabel, BorderLayout.CENTER);
        contentPanel.add(timeLabel, BorderLayout.SOUTH);

        bubble.add(contentPanel);
        bubble.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));
        messagePanel.add(bubble);
        chatPanel.add(messagePanel);
    }

    private void clearChatPanel() {
        chatPanel.removeAll();
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private String getChatId(String email1, String email2) {
        List<String> emails = Arrays.asList(email1, email2);
        Collections.sort(emails);
        return emails.get(0).replace(".", "_") + "_" + emails.get(1).replace(".", "_");
    }

    private void showEmojiPicker() {
        String[] emojis = {"😊", "😂", "❤️", "👍", "😍", "😭", "😘", "🤔", "😎", "🔥", "💯", "🎉"};
        JPopupMenu emojiMenu = new JPopupMenu();
        JPanel emojiPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        emojiPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            emojiBtn.setBorderPainted(false);
            emojiBtn.setContentAreaFilled(false);
            emojiBtn.setFocusPainted(false);
            emojiBtn.addActionListener(e -> {
                int pos = inputField.getCaretPosition();
                String oldText = inputField.getText();
                inputField.setText(oldText.substring(0, pos) + emoji + oldText.substring(pos));
                inputField.requestFocus();
                inputField.setCaretPosition(pos + emoji.length());
                emojiMenu.setVisible(false);
            });
            emojiPanel.add(emojiBtn);
        }
        emojiMenu.add(emojiPanel);
        emojiMenu.revalidate();
        emojiMenu.repaint();
        emojiMenu.show(emojiButton, 0, -emojiMenu.getPreferredSize().height);
    }

    private void logout() {
        // Create custom themed dialog
        JDialog dialog = new JDialog(this, "Logout", true);
        dialog.setSize(380, 210);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        dialog.setResizable(false);

        // Main content panel with rounded background
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Title label
        JLabel titleLabel = new JLabel("Logout Confirmation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.LEFT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 6, 0));

        // Icon and message panel (centered vertically)
        JPanel rowPanel = new JPanel();
        rowPanel.setOpaque(false);
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Warning icon
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_COLOR.brighter(), 0, getHeight(), ACCENT_COLOR.darker());
                g2.setPaint(gp);
                g2.fillOval(0, 0, 45, 45);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval(2, 2, 45, 45);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (45 - fm.stringWidth("!")) / 2;
                int y = (45 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString("!", x, y);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(45, 45));
        iconPanel.setMinimumSize(new Dimension(45, 45));
        iconPanel.setMaximumSize(new Dimension(45, 45));
        iconPanel.setOpaque(false);
        iconPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        iconPanel.setBorder(new EmptyBorder(0, 8, 0, 20)); // left and right margin

        // Message text
        JLabel messageLabel = new JLabel("<html><div style='font-size:13px; color:#34495e; width:220px;'>Are you sure you want to logout?</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
        messageLabel.setBorder(new EmptyBorder(0, 12, 0, 0));
       

        rowPanel.add(iconPanel);
        rowPanel.add(messageLabel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelButton = new JButton("Cancel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(BORDER_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(BACKGROUND_COLOR);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setForeground(TEXT_COLOR);
        cancelButton.setPreferredSize(new Dimension(85, 35));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton logoutButton = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(ACCENT_COLOR.brighter());
                } else {
                    g2.setColor(ACCENT_COLOR);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setPreferredSize(new Dimension(85, 35));
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> {
            dialog.dispose();
            performLogout();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(logoutButton);

        // Add components to content panel
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(rowPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private void performLogout() {
        // Stop message listener
        stopMessageListener();
        
        // Close current window
        this.dispose();
        
        // Open login interface
        SwingUtilities.invokeLater(() -> {
            LoginInterface loginInterface = new LoginInterface();
            loginInterface.setVisible(true);
        });
    }

    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "My Profile", true);
        dialog.setSize(370, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        dialog.setResizable(false);

        // Use BoxLayout for vertical alignment
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(24, 32, 24, 32));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(18));

        // Avatar preview (perfectly round)
        int avatarSize = 90;
        String currentAvatar = userCache.containsKey(currentUserEmail) ? userCache.get(currentUserEmail).avatar : "";
        File avatarFile = (currentAvatar != null && !currentAvatar.isEmpty()) ? new File(currentAvatar) : null;
        String initial = currentUserEmail.substring(0, 1).toUpperCase();
        JPanel avatarPanel = new JPanel() {
            Image avatarImg = null;
            boolean hasImage = false;
            {
                setPreferredSize(new Dimension(avatarSize, avatarSize));
                setMaximumSize(new Dimension(avatarSize, avatarSize));
                setMinimumSize(new Dimension(avatarSize, avatarSize));
                setOpaque(false);
                if (avatarFile != null && avatarFile.exists()) {
                    try {
                        avatarImg = new ImageIcon(avatarFile.getPath()).getImage().getScaledInstance(avatarSize, avatarSize, Image.SCALE_SMOOTH);
                        hasImage = true;
                    } catch (Exception e) {
                        hasImage = false;
                    }
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, avatarSize, avatarSize);
                if (hasImage && avatarImg != null) {
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, avatarSize, avatarSize));
                    g2.drawImage(avatarImg, 0, 0, avatarSize, avatarSize, null);
                } else {
                    g2.setColor(PRIMARY_COLOR);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (avatarSize - fm.stringWidth(initial)) / 2;
                    int y = (avatarSize + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(initial, x, y);
                }
                    g2.dispose();
                }
        };
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(avatarPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Upload button with loading indicator
        JPanel uploadPanel = new JPanel();
        uploadPanel.setLayout(new BoxLayout(uploadPanel, BoxLayout.X_AXIS));
        uploadPanel.setOpaque(false);
        JButton uploadButton = new JButton("Upload Picture");
        styleDialogButton(uploadButton, SECONDARY_COLOR, SECONDARY_COLOR, Color.WHITE);
        uploadButton.setPreferredSize(new Dimension(160, 38));
        uploadButton.setMaximumSize(new Dimension(160, 38));
        uploadButton.setMinimumSize(new Dimension(160, 38));
        JProgressBar uploadProgress = new JProgressBar();
        uploadProgress.setIndeterminate(true);
        uploadProgress.setVisible(false);
        uploadProgress.setPreferredSize(new Dimension(30, 30));
        uploadPanel.add(Box.createHorizontalGlue());
        uploadPanel.add(uploadButton);
        uploadPanel.add(Box.createHorizontalStrut(8));
        uploadPanel.add(uploadProgress);
        uploadPanel.add(Box.createHorizontalGlue());
        uploadPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(uploadPanel);
        contentPanel.add(Box.createVerticalStrut(18));

        // Store the selected avatar path
        final String[] selectedAvatarPath = {currentAvatar};

        uploadButton.addActionListener(e -> {
            CustomFilePicker picker = new CustomFilePicker(this, "Select Image", new File(System.getProperty("user.home")));
            picker.setVisible(true);
            File file = picker.getSelectedFile();
            if (file != null) {
                uploadButton.setEnabled(false);
                uploadProgress.setVisible(true);
                new Thread(() -> {
                    try {
                        String userId = currentUserEmail.replaceAll("[^a-zA-Z0-9]", "_");
                        String imageUrl = uploadProfilePictureToFirebase(file, userId);
                        selectedAvatarPath[0] = imageUrl;
                        SwingUtilities.invokeLater(() -> {
                            // Update avatarPanel to show new image
                            ((JPanel)avatarPanel).removeAll();
                            avatarPanel.repaint();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "Failed to upload image to Firebase Storage.", "Error", JOptionPane.ERROR_MESSAGE));
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            uploadButton.setEnabled(true);
                            uploadProgress.setVisible(false);
                        });
                    }
                }).start();
            }
        });

        // Display name
        JLabel nameLabel = new JLabel("Display Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameField.setText(currentUserEmail.split("@")[0]);
        nameField.setBorder(new EmptyBorder(8, 10, 8, 10));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameField);
        contentPanel.add(Box.createVerticalStrut(10));

        // Email (not editable)
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(TEXT_COLOR);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(emailLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        JLabel emailValue = new JLabel(currentUserEmail);
        emailValue.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emailValue.setForeground(TEXT_COLOR);
        emailValue.setBorder(new EmptyBorder(8, 10, 8, 10));
        emailValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(emailValue);
        contentPanel.add(Box.createVerticalStrut(18));

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        JButton saveButton = new JButton("Save");
        styleDialogButton(saveButton, PRIMARY_COLOR, PRIMARY_COLOR, Color.WHITE);
        saveButton.setPreferredSize(new Dimension(120, 38));
        saveButton.setMaximumSize(new Dimension(120, 38));
        saveButton.setMinimumSize(new Dimension(120, 38));
        JButton deleteButton = new JButton("Delete Profile");
        styleDialogButton(deleteButton, ACCENT_COLOR, ACCENT_COLOR, Color.WHITE);
        deleteButton.setPreferredSize(new Dimension(120, 38));
        deleteButton.setMaximumSize(new Dimension(120, 38));
        deleteButton.setMinimumSize(new Dimension(120, 38));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(buttonPanel);

        // Save and delete actions (same as before)
        saveButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient();
                    String databaseUrl = FirebaseAuthService.getDatabaseUrl();
                    String email = currentUserEmail;
                    String getUrl = databaseUrl + "/users.json?orderBy=\"email\"&equalTo=\"" + email + "\"";
                    Request getRequest = new Request.Builder().url(getUrl).get().build();
                    Response getResponse = client.newCall(getRequest).execute();
                    String getResponseBody = getResponse.body().string();
                    JSONObject usersJson = new JSONObject(getResponseBody);
                    if (usersJson.length() > 0) {
                        String userKey = usersJson.keys().next();
                        JSONObject userObj = usersJson.getJSONObject(userKey);
                        userObj.put("displayName", nameField.getText().trim());
                        userObj.put("avatar", selectedAvatarPath[0] != null ? selectedAvatarPath[0] : "");
                        String putUrl = databaseUrl + "/users/" + userKey + ".json";
                        RequestBody putBody = RequestBody.create(userObj.toString(), MediaType.parse("application/json; charset=utf-8"));
                        Request putRequest = new Request.Builder().url(putUrl).put(putBody).build();
                        client.newCall(putRequest).execute();
                        SwingUtilities.invokeLater(() -> {
                            dialog.dispose();
                            fetchUsersAndGroups();
                            chatList.repaint();
                            JOptionPane.showMessageDialog(this, "Profile updated!\nIf you don't see the change, please restart the app.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "User not found in database.", "Error", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });
        deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete your profile? This cannot be undone.", "Delete Profile", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                new Thread(() -> {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        String databaseUrl = FirebaseAuthService.getDatabaseUrl();
                        String email = currentUserEmail;
                        String getUrl = databaseUrl + "/users.json?orderBy=\"email\"&equalTo=\"" + email + "\"";
                        Request getRequest = new Request.Builder().url(getUrl).get().build();
                        Response getResponse = client.newCall(getRequest).execute();
                        String getResponseBody = getResponse.body().string();
                        JSONObject usersJson = new JSONObject(getResponseBody);
                        if (usersJson.length() > 0) {
                            String userKey = usersJson.keys().next();
                            String deleteUrl = databaseUrl + "/users/" + userKey + ".json";
                            Request deleteRequest = new Request.Builder().url(deleteUrl).delete().build();
                            client.newCall(deleteRequest).execute();
                            SwingUtilities.invokeLater(() -> {
                                dialog.dispose();
                                JOptionPane.showMessageDialog(this, "Profile deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                                logout();
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Failed to delete profile.", "Error", JOptionPane.ERROR_MESSAGE));
                    }
                }).start();
            }
        });

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // Helper to show JFileChooser with guaranteed system LookAndFeel
    private java.io.File showSystemFileChooser(Component parent) {
        java.io.File selectedFile = null;
        String oldLF = UIManager.getLookAndFeel().getClass().getName();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) { }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
        }
        try {
            UIManager.setLookAndFeel(oldLF);
        } catch (Exception ex) { }
        // Update the parent and file chooser UI
        SwingUtilities.updateComponentTreeUI(parent);
        SwingUtilities.updateComponentTreeUI(fileChooser);
        return selectedFile;
    }

    // Helper to upload profile picture to Firebase Storage
    public String uploadProfilePictureToFirebase(File file, String userId) throws Exception {
        Bucket bucket = StorageClient.getInstance().bucket();
        String blobString = "profile_pictures/" + userId + ".png";
        Blob blob = bucket.create(blobString, new java.io.FileInputStream(file), "image/png");
        blob.createAcl(com.google.cloud.storage.Acl.of(com.google.cloud.storage.Acl.User.ofAllUsers(), com.google.cloud.storage.Acl.Role.READER));
        return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), blobString);
    }

    // Helper to set a circular avatar from a URL
    private void setCircularAvatarFromUrl(JLabel label, String imageUrl, int size) {
        try {
            ImageIcon icon = new ImageIcon(new java.net.URL(imageUrl));
            Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            BufferedImage circleBuffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleBuffer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(img, 0, 0, size, size, null);
            g2.dispose();
            label.setIcon(new ImageIcon(circleBuffer));
            label.setText("");
        } catch (Exception e) {
            label.setIcon(null);
            label.setText("?");
        }
    }

    private void sendFileToCurrentChat() {
        System.out.println("File button clicked!");
        CustomFilePicker picker = new CustomFilePicker(this, "Select File", new File(System.getProperty("user.home")));
        picker.setVisible(true);
        File file = picker.getSelectedFile();
        if (file != null) {
            new Thread(() -> {
                try {
                    String userId = currentUserEmail.replaceAll("[^a-zA-Z0-9]", "_");
                    String fileName = file.getName();
                    String storagePath = "chat_files/" + userId + "_" + System.currentTimeMillis() + "_" + fileName;
                    Bucket bucket = StorageClient.getInstance().bucket();
                    Blob blob = bucket.create(storagePath, new java.io.FileInputStream(file), "application/octet-stream");
                    blob.createAcl(com.google.cloud.storage.Acl.of(com.google.cloud.storage.Acl.User.ofAllUsers(), com.google.cloud.storage.Acl.Role.READER));
                    String fileUrl = String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), storagePath);
                    sendFileMessageToChat(fileName, fileUrl, file.length());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Failed to upload file.", "Error", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        }
    }

    private void sendFileMessageToChat(String fileName, String fileUrl, long fileSize) {
        if (currentChat == null) return;
        try {
            OkHttpClient client = new OkHttpClient();
            String databaseUrl = FirebaseAuthService.getDatabaseUrl();
            String chatId;
            String toField;
            if (currentChat.isGroup) {
                GroupInfo group = (GroupInfo) currentChat.chatInfo;
                chatId = "group_" + group.id;
                toField = chatId;
            } else {
                UserInfo user = (UserInfo) currentChat.chatInfo;
                chatId = getChatId(currentUserEmail, user.email);
                toField = user.email;
            }
            long timestamp = System.currentTimeMillis();
            String url = databaseUrl + "/messages/" + chatId + "/" + timestamp + ".json";
            JSONObject msgObj = new JSONObject();
            msgObj.put("from", currentUserEmail);
            msgObj.put("to", toField);
            msgObj.put("timestamp", timestamp);
            msgObj.put("type", "file");
            msgObj.put("fileName", fileName);
            msgObj.put("fileUrl", fileUrl);
            msgObj.put("fileSize", fileSize);
            RequestBody body = RequestBody.create(msgObj.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).put(body).build();
            client.newCall(request).execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "LinkApp");
        SwingUtilities.invokeLater(() -> {
            new ChatInterface("testuser@gmail.com").setVisible(true);
        });
    }
}