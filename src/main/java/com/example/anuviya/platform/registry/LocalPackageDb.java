package com.example.anuviya.platform.registry;

import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class LocalPackageDb {
    private static final String FILE_NAME = "installed_packages.properties";
    private static final File DB_FILE;

    static {
        String userHome = System.getProperty("user.home");
        File parentDir = new File(userHome, ".gemini/antigravity");
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        DB_FILE = new File(parentDir, FILE_NAME);
    }

    public static synchronized void register(String packageId, String version) {
        Properties props = loadProperties();
        props.setProperty(packageId, version);
        saveProperties(props);
    }

    public static synchronized void unregister(String packageId) {
        Properties props = loadProperties();
        props.remove(packageId);
        saveProperties(props);
    }

    public static synchronized boolean isInstalled(String packageId) {
        // Try reading InstallerDef from package catalog for generic verification
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(packageId);
        if (pkgOpt.isPresent()) {
            ServicePackage pkg = pkgOpt.get();
            String osName = getNormalizedOsName();
            ServicePackage.InstallerDef instDef = pkg.installerByOs().get(osName);
            if (instDef != null) {
                // 1. Check Local AppData
                if (instDef.verifyExecutable() != null) {
                    String localAppData = System.getenv("LOCALAPPDATA");
                    if (localAppData != null) {
                        File file = new File(localAppData, instDef.verifyExecutable());
                        if (file.exists()) {
                            return true;
                        }
                    }
                    
                    // 2. Check Program Files
                    String programFiles = System.getenv("ProgramFiles");
                    if (programFiles != null) {
                        File file1 = new File(programFiles, instDef.verifyExecutable());
                        if (file1.exists()) {
                            return true;
                        }
                        String stripped = instDef.verifyExecutable().replace("Programs/", "");
                        File file2 = new File(programFiles, stripped);
                        if (file2.exists()) {
                            return true;
                        }
                    }
                }

                // 3. Check System PATH (via verifyCommand)
                if (instDef.verifyCommand() != null) {
                    try {
                        Process process = Runtime.getRuntime().exec(instDef.verifyCommand());
                        process.waitFor();
                        if (process.exitValue() == 0) {
                            return true;
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        
        Properties props = loadProperties();
        return props.containsKey(packageId);
    }

    public static synchronized List<String> getInstalledPackageIds() {
        Properties props = loadProperties();
        List<String> list = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            list.add(key);
        }
        
        // Scan all packages from registry to see if they are installed externally
        for (ServicePackage pkg : PackageRegistry.getInstance().all()) {
            if (!list.contains(pkg.id()) && isInstalled(pkg.id())) {
                list.add(pkg.id());
            }
        }
        return list;
    }

    private static String getNormalizedOsName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "windows";
        if (os.contains("mac")) return "mac";
        return "linux";
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        if (DB_FILE.exists()) {
            try (FileInputStream fis = new FileInputStream(DB_FILE)) {
                props.load(fis);
            } catch (IOException e) {
                // Ignore and return empty
            }
        }
        return props;
    }

    private static void saveProperties(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(DB_FILE)) {
            props.store(fos, "Bodhak Installed Packages Database");
        } catch (IOException e) {
            System.err.println("Error saving package DB: " + e.getMessage());
        }
    }
}
