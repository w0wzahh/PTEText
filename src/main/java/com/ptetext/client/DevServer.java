package com.ptetext.client;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/** Ensures the PTEText API is running on localhost:8080, starting it via scripts/run.bat if not. */
public class DevServer {

    private static final int SERVER_PORT = 8080;
    private static final int MYSQL_PORT = 3306;
    private static final int WAIT_SECONDS = 120;

    public static void main(String[] args) {
        ensureRunning();
    }

    public static void ensureRunning() {
        if (isUp(SERVER_PORT)) {
            System.out.println("[dev] Server already running.");
            return;
        }
        if (!isUp(MYSQL_PORT)) {
            System.out.println("[dev] Warning: MySQL isn't answering on " + MYSQL_PORT
                    + " - start it in XAMPP or the server will fail.");
        }

        File script = findRunBat();
        if (script == null) {
            System.out.println("[dev] scripts/run.bat not found - start the server manually.");
            return;
        }

        System.out.println("[dev] Starting " + script + " ...");
        try {
            new ProcessBuilder("cmd", "/c", "start", "\"PTEText server\"",
                    "cmd", "/k", "\"" + script.getAbsolutePath() + "\"")
                    .directory(script.getParentFile())
                    .start();
        } catch (Exception e) {
            System.out.println("[dev] Could not start server: " + e.getMessage());
            return;
        }

        for (int waited = 0; waited < WAIT_SECONDS * 1000; waited += 500) {
            if (isUp(SERVER_PORT)) {
                System.out.println("[dev] Server is up.");
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("[dev] No response on " + SERVER_PORT + " after " + WAIT_SECONDS
                + "s - check the server window for errors.");
    }

    private static File findRunBat() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path script = dir.resolve("scripts/run.bat");
            if (Files.isRegularFile(script)) {
                return script.toFile();
            }
            dir = dir.getParent();
        }
        try {
            Path classes = Path.of(DevServer.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path script = classes.getParent().getParent().resolve("scripts/run.bat");
            if (Files.isRegularFile(script)) {
                return script.toFile();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isUp(int port) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", port), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
