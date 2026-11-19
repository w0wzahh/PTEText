package com.ptetext.client;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * DEV HELPER: starts the PTEText server (scripts/run.bat) if it isn't already
 * running. Safe to delete, together with the one call in ConsoleClient.main.
 */
public class DevServer {

    private static final String HOST = "localhost";
    private static final int SERVER_PORT = 8080; // server.port in application.properties
    private static final int MYSQL_PORT = 3306;  // XAMPP MySQL, the server needs it

    // "mvn spring-boot:run" compiles first, so give it time
    private static final int WAIT_SECONDS = 120;

    // Last-resort location, only used if run.bat can't be found from the working directory
    private static final Path FALLBACK = Path.of(
            System.getProperty("user.home"),
            "Desktop", "Projects", "PTEText", "scripts", "run.bat");

    public static void ensureRunning() {
        if (isUp(SERVER_PORT)) {
            System.out.println("[dev] Server already running.");
            return;
        }

        if (!isUp(MYSQL_PORT)) {
            System.out.println("[dev] Warning: MySQL isn't answering on port " + MYSQL_PORT
                    + ". Start it in XAMPP, otherwise the server will fail to start.");
        }

        File script = findRunBat();
        if (script == null) {
            System.out.println("[dev] Could not find scripts/run.bat, start the server manually.");
            return;
        }

        System.out.println("[dev] Server not running, starting " + script + " ...");
        try {
            // Own console window; /k keeps it open if the server crashes so you can read why.
            // run.bat does "cd /d %~dp0.." itself, so the working directory doesn't matter.
            new ProcessBuilder("cmd", "/c", "start", "\"PTEText server\"",
                    "cmd", "/k", "\"" + script.getAbsolutePath() + "\"")
                    .directory(script.getParentFile())
                    .start();

            for (int i = 0; i < WAIT_SECONDS * 2; i++) {
                Thread.sleep(500);
                if (isUp(SERVER_PORT)) {
                    System.out.println("[dev] Server is up.");
                    return;
                }
            }
            System.out.println("[dev] Waited " + WAIT_SECONDS + "s and nothing is on port " + SERVER_PORT
                    + ". Check the server window for errors.");
        } catch (Exception e) {
            System.out.println("[dev] Could not start server: " + e);
        }
    }

    /** Walks up from the working directory (NetBeans uses the project root) looking for scripts/run.bat. */
    private static File findRunBat() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("scripts").resolve("run.bat");
            if (Files.isRegularFile(candidate)) {
                return candidate.toFile();
            }
            dir = dir.getParent();
        }
        return Files.isRegularFile(FALLBACK) ? FALLBACK.toFile() : null;
    }

    private static boolean isUp(int port) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(HOST, port), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}