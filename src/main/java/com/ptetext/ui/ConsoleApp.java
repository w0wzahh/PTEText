package com.ptetext.ui;

import com.ptetext.config.DatabaseConfig;
import com.ptetext.dao.UserDao;
import com.ptetext.db.ConnectionFactory;
import com.ptetext.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * The menus — mostly a skeleton right now.
 *
 * What works: the startup DB health check and "List users" — the reference
 * feature. Follow its path (UI -> DAO -> MySQL) when you build yours.
 *
 * Everything else is a TODO mapped to a GitHub issue. Implement it, wire it
 * into the menu, delete the stub line.
 */
public class ConsoleApp {

    private final Scanner scanner = new Scanner(System.in);

    private final ConnectionFactory factory;
    private final UserDao userDao;
    // TODO(team): construct your DAOs/services here as they get built

    public ConsoleApp() {
        DatabaseConfig config = DatabaseConfig.load();
        this.factory = new ConnectionFactory(config);
        this.userDao = new UserDao(factory);
    }

    public void run() {
        System.out.println("========================================");
        System.out.println("   PTEText - console messenger");
        System.out.println("========================================");
        System.out.println("Database status:");
        System.out.print(factory.healthCheck());

        boolean running = true;
        while (running) {
            running = menu();
        }
        System.out.println("Bye!");
    }

    private boolean menu() {
        System.out.println("\n1) List users            (works - the example feature)");
        System.out.println("2) Register              (TODO - issue #9)");
        System.out.println("3) Login                 (TODO - issue #10)");
        System.out.println("4) My conversations      (TODO - issue #11)");
        System.out.println("5) New group chat        (TODO - issue #12)");
        System.out.println("6) My contacts           (TODO - issue #13)");
        System.out.println("7) Recent activity       (TODO - issue #14)");
        System.out.println("0) Exit");
        switch (prompt("Choose")) {
            case "1" -> listUsers();
            case "2", "3", "4", "5", "6", "7" -> notImplemented();
            case "0" -> { return false; }
            default -> System.out.println("Unknown option.");
        }
        return true;
    }

    /**
     * REFERENCE FEATURE — fully built so you can copy the pattern:
     * menu calls DAO, DAO runs SQL, rows map to model records.
     */
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

    private void printDbError(SQLException e) {
        System.out.println("Database error: " + e.getMessage());
        System.out.println("Is XAMPP MySQL running? See docs/04-setup-guide.md.");
    }
}
