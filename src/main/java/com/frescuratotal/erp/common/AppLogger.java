package com.frescuratotal.erp.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class AppLogger {
    private static boolean configured;

    private AppLogger() {
    }

    public static synchronized void configure() {
        if (configured) {
            return;
        }
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler handler = new FileHandler("logs/frescura-total.log", true);
            handler.setFormatter(new SimpleFormatter());
            Logger root = Logger.getLogger("");
            root.addHandler(handler);
            root.setLevel(Level.INFO);
            configured = true;
        } catch (IOException e) {
            Logger.getLogger(AppLogger.class.getName())
                    .log(Level.WARNING, "Could not enable file logging.", e);
        }
    }
}
