import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import UI.RoundedBorder;
import javax.swing.Timer;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class ChatInterface extends JFrame {
    private JPanel chatPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JButton emojiButton;
    private JList<UserInfo> userList;
    private DefaultListModel<UserInfo> userListModel;
    private String currentUserEmail;
    private UserInfo chattingWith;
    private JLabel chattingWithLabel;
    private JLabel onlineStatusLabel;
    private JScrollPane chatScrollPane;
    private ScheduledExecutorService scheduler;
    private String lastLoadedChatId = null;
    private JTextField searchField;
    private Map<String, UserInfo> userCache = new HashMap<>();

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

    public ChatInterface(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
        setupLookAndFeel();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        fetchUsers();
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
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        chatPanel = new JPanel();
        inputField = new JTextField();
        sendButton = new JButton();
        emojiButton = new JButton();
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
        JScrollPane userScrollPane = createUserList();
        leftPanel.add(header, BorderLayout.NORTH);
        leftPanel.add(searchPanel, BorderLayout.CENTER);
        leftPanel.add(userScrollPane, BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createLeftHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 20, 15, 20));
        JLabel titleLabel = new JLabel("Messages");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        JButton newChatButton = new JButton(FontIcon.of(FontAwesome.PENCIL, 20, PRIMARY_COLOR));
        newChatButton.setBorderPainted(false);
        newChatButton.setContentAreaFilled(false);
        newChatButton.setFocusPainted(false);
        newChatButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(titleLabel, BorderLayout.WEST);
        header.add(newChatButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new EmptyBorder(0, 20, 15, 20));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(BACKGROUND_COLOR);
        searchField.setBorder(new EmptyBorder(12, 16, 12, 16));
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
        searchWrapper.add(searchField);
        searchWrapper.setPreferredSize(new Dimension(0, 40));
        searchPanel.add(searchWrapper);
        return searchPanel;
    }

    private JScrollPane createUserList() {
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userList.setFixedCellHeight(72);
        userList.setCellRenderer(new ModernUserListRenderer());
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBackground(Color.WHITE);
        userList.setBorder(null);
        JScrollPane scrollPane = new JScrollPane(userList);
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
        JButton videoCall = createHeaderButton(FontIcon.of(FontAwesome.VIDEO_CAMERA, 20, PRIMARY_COLOR));
        JButton voiceCall = createHeaderButton(FontIcon.of(FontAwesome.PHONE, 20, PRIMARY_COLOR));
        JButton moreOptions = createHeaderButton(FontIcon.of(FontAwesome.ELLIPSIS_H, 20, PRIMARY_COLOR));
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
        inputField.setBorder(new EmptyBorder(12, 20, 12, 15));
        inputField.setBackground(BACKGROUND_COLOR);
        inputField.putClientProperty("JTextField.placeholderText", "Type a message...");
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
        buttonContainer.add(emojiButton);
        buttonContainer.add(sendButton);
        inputContainer.add(inputField, BorderLayout.CENTER);
        inputContainer.add(buttonContainer, BorderLayout.EAST);
        inputArea.add(inputContainer);
        return inputArea;
    }

    private void setupEventListeners() {
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                chattingWith = userList.getSelectedValue();
                if (chattingWith != null) {
                    chattingWithLabel.setText(chattingWith.name);
                    onlineStatusLabel.setText(chattingWith.status.equals("online") ? "● Online" : "Last seen recently");
                    onlineStatusLabel.setForeground(chattingWith.status.equals("online") ? ONLINE_GREEN : TEXT_COLOR);
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopMessageListener();
                System.exit(0);
            }
        });
    }

    private void filterUsers() {
        String query = searchField.getText().toLowerCase().trim();
        userListModel.clear();
        for (UserInfo user : userCache.values()) {
            if (query.isEmpty() || user.name.toLowerCase().contains(query) || user.email.toLowerCase().contains(query)) {
                userListModel.addElement(user);
            }
        }
    }

    private void fetchUsers() {
        SwingUtilities.invokeLater(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String databaseUrl = FirebaseAuthService.getDatabaseUrl();
                String url = databaseUrl + "/users.json";
                Request request = new Request.Builder().url(url).get().build();
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                userListModel.clear();
                userCache.clear();
                for (String key : json.keySet()) {
                    JSONObject userObj = json.getJSONObject(key);
                    String email = userObj.optString("email", "");
                    if (!email.equals(currentUserEmail)) {
                        UserInfo userInfo = new UserInfo(
                            email,
                            userObj.optString("displayName", email.split("@")[0]),
                            userObj.optString("status", "offline"),
                            userObj.optString("avatar", ""),
                            userObj.optLong("lastSeen", System.currentTimeMillis())
                        );
                        userListModel.addElement(userInfo);
                        userCache.put(email, userInfo);
                    }
                }
            } catch (IOException | JSONException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && chattingWith != null) {
            try {
                OkHttpClient client = new OkHttpClient();
                String databaseUrl = FirebaseAuthService.getDatabaseUrl();
                String chatId = getChatId(currentUserEmail, chattingWith.email);
                long timestamp = System.currentTimeMillis();
                String url = databaseUrl + "/messages/" + chatId + "/" + timestamp + ".json";
                JSONObject msgObj = new JSONObject();
                msgObj.put("from", currentUserEmail);
                msgObj.put("to", chattingWith.email);
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
        if (chattingWith == null) return;
        String chatId = getChatId(currentUserEmail, chattingWith.email);
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
                    msgObj.optLong("timestamp", 0)
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
        JLabel textLabel = new JLabel("<html><div style='max-width: 280px; word-wrap: break-word;'>" + 
                                     msg.text.replace("\n", "<br>") + "</div></html>");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textLabel.setForeground(isMe ? Color.WHITE : TEXT_COLOR);
        JLabel timeLabel = new JLabel(new SimpleDateFormat("h:mm a").format(new Date(msg.timestamp)));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(isMe ? new Color(255, 255, 255, 180) : TEXT_COLOR);
        timeLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(textLabel, BorderLayout.CENTER);
        content.add(timeLabel, BorderLayout.SOUTH);
        bubble.add(content);
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

    // Data classes
    private static class Message {
        String from, to, text;
        long timestamp;
        Message(String from, String to, String text, long timestamp) {
            this.from = from;
            this.to = to;
            this.text = text;
            this.timestamp = timestamp;
        }
    }

    private static class UserInfo {
        String email, name, status, avatar;
        long lastSeen;
        UserInfo(String email, String name, String status, String avatar, long lastSeen) {
            this.email = email;
            this.name = name;
            this.status = status;
            this.avatar = avatar;
            this.lastSeen = lastSeen;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    private class ModernUserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            UserInfo user = (UserInfo) value;
            JPanel panel = new JPanel(new BorderLayout(12, 0));
            panel.setBorder(new EmptyBorder(16, 20, 16, 20));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? PRIMARY_COLOR : Color.WHITE);
            JPanel avatarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(PRIMARY_COLOR);
                    g2.fillOval(0, 0, 40, 40);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    String initial = user.name.substring(0, 1).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (40 - fm.stringWidth(initial)) / 2;
                    int y = (40 + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(initial, x, y);
                    g2.dispose();
                }
            };
            avatarPanel.setPreferredSize(new Dimension(40, 40));
            avatarPanel.setOpaque(false);
            JPanel userInfo = new JPanel(new BorderLayout());
            userInfo.setOpaque(false);
            JLabel nameLabel = new JLabel(user.name);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            nameLabel.setForeground(isSelected ? Color.WHITE : TEXT_COLOR);
            JLabel statusLabel = new JLabel(user.status.equals("online") ? "Online" : "Last seen recently");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            statusLabel.setForeground(isSelected ? new Color(255, 255, 255, 180) : TEXT_COLOR);
            userInfo.add(nameLabel, BorderLayout.NORTH);
            userInfo.add(statusLabel, BorderLayout.SOUTH);
            JLabel onlineIndicator = new JLabel("●");
            onlineIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            onlineIndicator.setForeground(user.status.equals("online") ? ONLINE_GREEN : Color.WHITE);
            onlineIndicator.setBorder(new EmptyBorder(0, 0, 0, 8));
            panel.add(avatarPanel, BorderLayout.WEST);
            panel.add(userInfo, BorderLayout.CENTER);
            panel.add(onlineIndicator, BorderLayout.EAST);
            return panel;
        }
    }

    private void showEmojiPicker() {
        String[] emojis = {"😊", "😂", "❤️", "👍", "😍", "😭", "😘", "🤔", "😎", "🔥", "💯", "🎉"};
        JPopupMenu emojiMenu = new JPopupMenu();
        JPanel emojiPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        emojiPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
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
        emojiMenu.show(emojiButton, 0, -emojiMenu.getPreferredSize().height);
    }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "LinkApp");
        SwingUtilities.invokeLater(() -> {
            new ChatInterface("testuser@gmail.com").setVisible(true);
        });
    }
}