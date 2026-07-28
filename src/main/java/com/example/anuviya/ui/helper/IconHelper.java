package com.example.anuviya.ui.helper;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility helper to manage branding icon images across Anuviya UI components.
 * 
 * Provides instant Taskbar & Window Icon registration using multi-resolution JavaFX images 
 * and Java AWT Taskbar native integration for zero-delay Windows taskbar rendering.
 */
public final class IconHelper {

    private static Image cachedPrimaryIcon = null;
    private static List<Image> cachedMultiResIcons = null;
    private static BufferedImage cachedAwtImage = null;
    private static boolean taskbarIconApplied = false;

    private static final String[] FILE_SYSTEM_ICON_PATHS = {
        "app_icon.png",
        "app_icon.jpg",
        "logo.png",
        "logo.jpg",
        "icons/app_icon.png",
        "icons/logo.png",
        "src/main/resources/icons/app_icon.png"
    };

    private static final String[] RESOURCE_ICON_PATHS = {
        "/icons/app_icon.png",
        "/icons/logo.png",
        "/icons/app_logo.png",
        "/icons/anuviya_icon.png",
        "/icons/health.png",
        "/icons/puzzle.png",
        "/icons/dependency.png"
    };

    private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 128, 256};

    private IconHelper() {}

    /**
     * Gets or loads multi-resolution JavaFX Image variants for instant OS window/taskbar matching.
     */
    public static synchronized List<Image> getAppIconVariants() {
        if (cachedMultiResIcons != null && !cachedMultiResIcons.isEmpty()) {
            return cachedMultiResIcons;
        }

        List<Image> icons = new ArrayList<>();

        // 1. Try relative file system paths first (for live updates without rebuild)
        for (String path : FILE_SYSTEM_ICON_PATHS) {
            try {
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    String fileUrl = file.toURI().toString();
                    Image primary = new Image(fileUrl);
                    if (!primary.isError()) {
                        cachedPrimaryIcon = primary;
                        for (int size : ICON_SIZES) {
                            icons.add(new Image(fileUrl, size, size, true, true));
                        }
                        try {
                            cachedAwtImage = ImageIO.read(file);
                        } catch (Exception ignored) {}
                        System.out.println("[Anuviya] Loaded multi-res brand icon from file system: " + file.getAbsolutePath());
                        cachedMultiResIcons = icons;
                        return cachedMultiResIcons;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Try classpath resources
        for (String path : RESOURCE_ICON_PATHS) {
            try {
                URL resourceUrl = IconHelper.class.getResource(path);
                if (resourceUrl != null) {
                    String urlStr = resourceUrl.toExternalForm();
                    Image primary = new Image(urlStr);
                    if (!primary.isError()) {
                        cachedPrimaryIcon = primary;
                        for (int size : ICON_SIZES) {
                            icons.add(new Image(urlStr, size, size, true, true));
                        }
                        try {
                            cachedAwtImage = ImageIO.read(resourceUrl);
                        } catch (Exception ignored) {}
                        System.out.println("[Anuviya] Loaded multi-res brand icon from classpath resource: " + path);
                        cachedMultiResIcons = icons;
                        return cachedMultiResIcons;
                    }
                }
            } catch (Exception ignored) {}
        }

        return icons;
    }

    /**
     * Returns the primary full-resolution icon image.
     */
    public static Image getAppIconImage() {
        if (cachedPrimaryIcon == null) {
            getAppIconVariants();
        }
        return cachedPrimaryIcon;
    }

    /**
     * Clears cached icons, forcing re-detection.
     */
    public static synchronized void clearCache() {
        cachedPrimaryIcon = null;
        cachedMultiResIcons = null;
        cachedAwtImage = null;
        taskbarIconApplied = false;
    }

    /**
     * Creates an ImageView of the brand logo if an icon image exists.
     */
    public static ImageView createLogoImageView(double size) {
        Image img = getAppIconImage();
        if (img == null || img.isError()) {
            return null;
        }
        ImageView iv = new ImageView(img);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    /**
     * Applies the application icon to the specified Stage window instantly.
     */
    public static void applyWindowIcon(Stage stage) {
        if (stage == null) return;
        List<Image> variants = getAppIconVariants();
        if (variants != null && !variants.isEmpty()) {
            stage.getIcons().clear();
            stage.getIcons().addAll(variants);
        }

        applySystemTaskbarIcon();
        WindowsDarkHeaderHelper.applyDarkHeader(stage);
    }

    /**
     * Sets native OS Taskbar icon via Java AWT Taskbar API for 0ms delay on Windows/macOS taskbars.
     */
    public static synchronized void applySystemTaskbarIcon() {
        if (taskbarIconApplied) return;
        taskbarIconApplied = true;

        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    if (cachedAwtImage == null) {
                        getAppIconVariants();
                    }
                    if (cachedAwtImage != null) {
                        taskbar.setIconImage(cachedAwtImage);
                        System.out.println("[Anuviya] Applied OS native Taskbar icon.");
                    }
                }
            }
        } catch (Throwable t) {
            // Silently ignore headless/unsupported environment errors
        }
    }
}
