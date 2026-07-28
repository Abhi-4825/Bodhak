package com.example.anuviya.ui.helper;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility helper to enable Windows 10/11 Immersive Dark Title Bar and custom caption colors.
 */
public final class WindowsDarkHeaderHelper {

    private interface DwmApi extends Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);
        int DwmSetWindowAttribute(HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE_OLD = 19;
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    private static final int DWMWA_CAPTION_COLOR = 35;
    private static final int DWMWA_TEXT_COLOR = 36;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Anuviya-DarkHeader-Scheduler");
        t.setDaemon(true);
        return t;
    });

    private WindowsDarkHeaderHelper() {}

    /**
     * Enables Windows Native Immersive Dark Mode Title Bar for the specified Stage.
     */
    public static void applyDarkHeader(Stage stage) {
        if (!Platform.isWindows() || stage == null) {
            return;
        }

        // Schedule attempts to allow OS native window creation to complete
        for (int delayMs : new int[]{50, 200, 500, 1000}) {
            scheduler.schedule(() -> {
                javafx.application.Platform.runLater(() -> applyDarkHeaderInternal(stage));
            }, delayMs, TimeUnit.MILLISECONDS);
        }
    }

    private static void applyDarkHeaderInternal(Stage stage) {
        try {
            String title = stage.getTitle();
            if (title == null || title.isEmpty()) {
                return;
            }

            HWND hwnd = User32.INSTANCE.FindWindow(null, title);
            if (hwnd == null) {
                return;
            }

            // Enable Immersive Dark Mode attribute (20 for Win10 2004+, 19 for Win10 1809-1909)
            IntByReference darkModeOn = new IntByReference(1);
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, darkModeOn, 4);
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_OLD, darkModeOn, 4);

            // Windows 11 Caption Color (#0E1415 in BGR format: 0x0015140E)
            IntByReference captionColor = new IntByReference(0x0015140E);
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, captionColor, 4);

            // Windows 11 Text Color (#00DAF3 in BGR format: 0x00F3DA00)
            IntByReference textColor = new IntByReference(0x00F3DA00);
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, textColor, 4);

        } catch (Throwable ignored) {
        }
    }
}
