package com.ptetext.ui;

import com.ptetext.config.DatabaseConfig;
import com.ptetext.dao.ActivityLogDao;
import com.ptetext.dao.SessionDao;
import com.ptetext.dao.UserDao;
import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.Session;
import com.ptetext.model.User;
import com.ptetext.service.AuthService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {

    private final Scanner scanner = new Scanner(System.in);

    private final ConnectionFactory factory;
    private final UserDao userDao;
    private final AuthService authService;
    // TODO(team): construct your DAOs/services here as they get built

    private Session session;

    public ConsoleApp() {
        DatabaseConfig config = DatabaseConfig.load();
        this.factory = new ConnectionFactory(config);
        this.userDao = new UserDao(factory);
        this.authService = new AuthService(
                userDao, new SessionDao(factory), new ActivityLogDao(factory));
    }

    public void run() {
        System.out.println("========================================");
        System.out.println("   PTEText - console messenger");
        System.out.println("========================================");
        System.out.println("Database status:");
        System.out.print(factory.healthCheck());

        boolean running = true;
        while (running) {
            running = session == null ? guestMenu() : userMenu();
        }
        System.out.println("Bye!");
    }

    private boolean guestMenu() {
        System.out.println("\n1) List users            (works - the example feature)");
        System.out.println("2) Register");
        System.out.println("3) Login");
        System.out.println("0) Exit");
        switch (prompt("Choose")) {
            case "1" -> listUsers();
            case "2" -> doRegister();
            case "3" -> doLogin();
            case "0" -> { return false; }
            default -> System.out.println("Unknown option.");
        }
        return true;
    }

    private boolean userMenu() {
        System.out.println("\n--- " + session.user().displayName()
                + " (@" + session.user().username() + ") ---");
        System.out.println("4) My conversations      (TODO - issue #11)");
        System.out.println("5) New group chat        (TODO - issue #12)");
        System.out.println("6) My contacts           (TODO - issue #13)");
        System.out.println("7) Recent activity       (TODO - issue #14)");
        System.out.println("0) Logout");
        try {
            switch (prompt("Choose")) {
                case "4", "5", "6", "7" -> notImplemented();
                case "0" -> logout();
                default -> System.out.println("Unknown option.");
            }
        } catch (SQLException e) {
            printDbError(e);
        }
        return true;
    }

    private void doRegister() {
        String username = prompt("Pick a username");
        String displayName = prompt("Display name (shown to others)");
        String password = promptSecret("Pick a password (min 6 chars)");
        try {
            User user = authService.register(username, password, displayName);
            System.out.println("Account created! You can now log in as @" + user.username());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private void doLogin() {
        String username = prompt("Username");
        String password = promptSecret("Password");
        try {
            Optional<Session> result = authService.login(username, password);
            if (result.isPresent()) {
                session = result.get();
                System.out.println("Welcome, " + session.user().displayName() + "!");
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private void logout() throws SQLException {
        authService.logout(session);
        session = null;
        System.out.println("Logged out.");
    }

    private void listUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("No users found - did you run sql/02_seed_data.sql?");
                return;
            }
            System.out.println("\nRegistered users:");
            for (User u : users) {
                System.out.printf("  @%-12s %s%n", u.username(), u.displayName());
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private void notImplemented() {
        System.out.println("Not built yet - check the Issues tab, this might be yours :)");
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine();
    }

    private String promptSecret(String label) {
        var console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(label + ": ");
            return chars == null ? "" : new String(chars);
        }
        return prompt(label);
    }

    private void printDbError(SQLException e) {
        System.out.println("Database error: " + e.getMessage());
        System.out.println("Is XAMPP MySQL running? See docs/04-setup-guide.md.");
    }
}
