package com.ptetext.ui;

import com.ptetext.config.DatabaseConfig;
import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.AttachmentDao;
import com.ptetext.dao.ContactDao;
import com.ptetext.dao.ConversationDao;
import com.ptetext.dao.MessageDao;
import com.ptetext.dao.SessionDao;
import com.ptetext.dao.UserDao;
import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.ActivityLogEntry;
import com.ptetext.model.Contact;
import com.ptetext.model.Session;
import com.ptetext.model.User;
import com.ptetext.service.AuthService;
import com.ptetext.service.ChatService;
import com.ptetext.service.ChatService.ChatMessage;
import com.ptetext.service.ChatService.ConversationSummary;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * All the menus. Keeps zero local state — every screen re-reads MySQL,
 * so two instances side by side can actually chat (smash /refresh).
 * TODO(team): this wants to be a real GUI eventually — see docs/05 for the backlog
 */
public class ConsoleApp {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final int MESSAGE_PAGE_SIZE = 15;

    private final Scanner scanner = new Scanner(System.in);

    private final AuthService authService;
    private final ChatService chatService;
    private final ContactDao contactDao;
    private final ActivityLogDao logDao;
    private final ConnectionFactory factory;

    private Session session;

    public ConsoleApp() {
        DatabaseConfig config = DatabaseConfig.load();
        this.factory = new ConnectionFactory(config);

        UserDao userDao = new UserDao(factory);
        SessionDao sessionDao = new SessionDao(factory);
        this.contactDao = new ContactDao(factory);
        ConversationDao conversationDao = new ConversationDao(factory);
        MessageDao messageDao = new MessageDao(factory);
        AttachmentDao attachmentDao = new AttachmentDao(factory);
        this.logDao = new ActivityLogDao(factory);

        this.authService = new AuthService(userDao, sessionDao, logDao);
        this.chatService = new ChatService(conversationDao, messageDao, userDao, attachmentDao, logDao);
    }

    public void run() {
        System.out.println("========================================");
        System.out.println("   PTEText - console messenger");
        System.out.println("========================================");
        System.out.println("Database status:");
        System.out.print(factory.healthCheck());

        boolean running = true;
        while (running) {
            if (session == null) {
                running = guestMenu();
            } else {
                userMenu();
            }
        }
        System.out.println("Bye!");
    }

    // ------------------------------------------------------------------
    // Guest menu (not logged in)
    // ------------------------------------------------------------------

    private boolean guestMenu() {
        System.out.println("\n1) Login");
        System.out.println("2) Register");
        System.out.println("0) Exit");
        switch (prompt("Choose")) {
            case "1" -> doLogin();
            case "2" -> doRegister();
            case "0" -> { return false; }
            default -> System.out.println("Unknown option.");
        }
        return true;
    }

    private void doLogin() {
        String username = prompt("Username");
        String password = prompt("Password");
        try {
            Optional<Session> result = authService.login(username, password);
            if (result.isPresent()) {
                session = result.get();
                System.out.println("Welcome, " + session.user().displayName() + "!");
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private void doRegister() {
        String username = prompt("Pick a username");
        String displayName = prompt("Display name (shown to others)");
        String password = prompt("Pick a password (min 6 chars)");
        try {
            User user = authService.register(username, password, displayName);
            System.out.println("Account created! You can now log in as @" + user.username());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ------------------------------------------------------------------
    // Logged-in menu
    // ------------------------------------------------------------------

    private void userMenu() {
        System.out.println("\n--- " + session.user().displayName()
                + " (@" + session.user().username() + ") ---");
        System.out.println("1) My conversations");
        System.out.println("2) New direct message");
        System.out.println("3) New group chat");
        System.out.println("4) My contacts");
        System.out.println("5) Add contact");
        System.out.println("6) Recent activity");
        System.out.println("0) Logout");
        try {
            switch (prompt("Choose")) {
                case "1" -> openConversationPicker();
                case "2" -> startDirectMessage();
                case "3" -> startGroupChat();
                case "4" -> showContacts();
                case "5" -> addContact();
                case "6" -> showActivity();
                case "0" -> logout();
                default -> System.out.println("Unknown option.");
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private void logout() throws SQLException {
        authService.logout(session);
        session = null;
        System.out.println("Logged out.");
    }

    // ------------------------------------------------------------------
    // Conversations
    // ------------------------------------------------------------------

    private void openConversationPicker() throws SQLException {
        List<ConversationSummary> conversations = chatService.listConversations(session.user().userId());
        if (conversations.isEmpty()) {
            System.out.println("No conversations yet. Start one with option 2 or 3.");
            return;
        }
        System.out.println("\nYour conversations:");
        for (int i = 0; i < conversations.size(); i++) {
            ConversationSummary c = conversations.get(i);
            System.out.printf("%2d) %s%s%n", i + 1, c.label(), c.group() ? "  [group]" : "");
        }
        Integer pick = promptInt("Open which? (0 = cancel)");
        if (pick == null || pick < 1 || pick > conversations.size()) {
            return;
        }
        chatLoop(conversations.get(pick - 1));
    }

    private void chatLoop(ConversationSummary conversation) throws SQLException {
        int conversationId = conversation.conversationId();
        printMessages(conversation);
        while (true) {
            String input = prompt("").trim();
            switch (input) {
                case "/back" -> { return; }
                case "/refresh" -> printMessages(conversation);
                case "" -> { /* ignore */ }
                default -> {
                    if (input.startsWith("/attach ")) {
                        String fileName = input.substring("/attach ".length()).trim();
                        chatService.sendAttachment(conversationId, session.user(), fileName, 0);
                        System.out.println("Attachment recorded: " + fileName);
                    } else {
                        chatService.sendMessage(conversationId, session.user(), input);
                    }
                    printMessages(conversation);
                }
            }
        }
    }

    private void printMessages(ConversationSummary conversation) throws SQLException {
        System.out.println("\n== " + conversation.label()
                + "  (conversation #" + conversation.conversationId() + ") ==");
        List<ChatMessage> messages = chatService.getMessages(
                conversation.conversationId(), MESSAGE_PAGE_SIZE);
        if (messages.isEmpty()) {
            System.out.println("  (no messages yet)");
        }
        for (ChatMessage m : messages) {
            String time = m.sentAt() != null ? m.sentAt().toLocalDateTime().format(TIME_FMT) : "?";
            System.out.printf("[%s] %s: %s%n", time, m.senderName(), m.body());
        }
        System.out.println("(type a message, /attach <file>, /refresh, /back)");
    }

    // ------------------------------------------------------------------
    // Starting new chats
    // ------------------------------------------------------------------

    private void startDirectMessage() throws SQLException {
        User other = pickUser("Message who? (0 = cancel)");
        if (other == null) {
            return;
        }
        int id = chatService.openOrCreateDirectChat(session.user().userId(), other.userId());
        chatLoop(new ConversationSummary(id, other.displayName(), false));
    }

    private void startGroupChat() throws SQLException {
        String title = prompt("Group title");
        List<User> others = pickUsers("Member numbers, comma-separated (e.g. 1,3)");
        if (others.isEmpty()) {
            System.out.println("A group needs at least one other member.");
            return;
        }
        List<Integer> memberIds = others.stream().map(User::userId).toList();
        int id = chatService.createGroup(title, session.user().userId(), memberIds);
        chatLoop(new ConversationSummary(id, title, true));
    }

    // ------------------------------------------------------------------
    // Contacts
    // ------------------------------------------------------------------

    private void showContacts() throws SQLException {
        List<Contact> contacts = contactDao.listContacts(session.user().userId());
        if (contacts.isEmpty()) {
            System.out.println("No contacts yet. Use option 5 to add one.");
            return;
        }
        System.out.println("\nYour contacts:");
        for (Contact c : contacts) {
            System.out.printf("  @%-12s %s%n", c.contactUsername(), c.label());
        }
    }

    private void addContact() throws SQLException {
        User other = pickUser("Add who? (0 = cancel)");
        if (other == null) {
            return;
        }
        String nickname = prompt("Nickname (optional, Enter to skip)");
        contactDao.addContact(session.user().userId(), other.userId(), nickname);
        logDao.log(session.user().userId(), "CONTACT_ADDED", "Added @" + other.username());
        System.out.println("Added @" + other.username() + " to your contacts.");
    }

    // ------------------------------------------------------------------
    // Activity log (ptetext_system)
    // ------------------------------------------------------------------

    private void showActivity() throws SQLException {
        List<ActivityLogEntry> entries = logDao.listRecent(10);
        System.out.println("\nRecent activity (all users):");
        for (ActivityLogEntry e : entries) {
            String who = e.userId() != null ? "user#" + e.userId() : "system";
            String time = e.createdAt() != null ? e.createdAt().toLocalDateTime().format(TIME_FMT) : "?";
            System.out.printf("  [%s] %-7s %-16s %s%n", time, who, e.action(),
                    e.details() != null ? e.details() : "");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private User pickUser(String question) throws SQLException {
        List<User> users = otherUsers();
        printUserList(users);
        Integer pick = promptInt(question);
        if (pick == null || pick < 1 || pick > users.size()) {
            return null;
        }
        return users.get(pick - 1);
    }

    private List<User> pickUsers(String question) throws SQLException {
        List<User> users = otherUsers();
        printUserList(users);
        String input = prompt(question);
        List<User> picked = new ArrayList<>();
        for (String part : input.split(",")) {
            try {
                int idx = Integer.parseInt(part.trim());
                if (idx >= 1 && idx <= users.size()) {
                    picked.add(users.get(idx - 1));
                }
            } catch (NumberFormatException ignored) {
                // skip bad input
            }
        }
        return picked;
    }

    private List<User> otherUsers() throws SQLException {
        return chatService.listAllUsers().stream()
                .filter(u -> u.userId() != session.user().userId())
                .toList();
    }

    private void printUserList(List<User> users) {
        System.out.println("\nRegistered users:");
        for (int i = 0; i < users.size(); i++) {
            System.out.printf("%2d) @%-12s %s%n", i + 1, users.get(i).username(),
                    users.get(i).displayName());
        }
    }

    private String prompt(String label) {
        System.out.print(label.isEmpty() ? "> " : label + ": ");
        return scanner.nextLine();
    }

    private Integer promptInt(String label) {
        try {
            return Integer.parseInt(prompt(label).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void printDbError(SQLException e) {
        System.out.println("Database error: " + e.getMessage());
        System.out.println("Is XAMPP MySQL running? See docs/04-setup-guide.md.");
    }
}
