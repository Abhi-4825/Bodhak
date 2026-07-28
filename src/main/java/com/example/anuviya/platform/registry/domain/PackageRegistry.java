package com.example.anuviya.platform.registry.domain;

import com.example.anuviya.platform.PackageCategory;
import com.example.anuviya.platform.ServicePackage;

import java.util.*;

public class PackageRegistry {
    private static final PackageRegistry INSTANCE = new PackageRegistry();
    private final Map<String, ServicePackage> packages = new LinkedHashMap<>();

    private PackageRegistry() {}

    public static PackageRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void register(ServicePackage pkg) {
        packages.put(pkg.id(), pkg);
    }

    public synchronized void clear() {
        packages.clear();
    }

    public synchronized Optional<ServicePackage> get(String id) {
        return Optional.ofNullable(packages.get(id));
    }

    public synchronized List<ServicePackage> all() {
        return new ArrayList<>(packages.values());
    }

    public synchronized List<ServicePackage> byCategory(PackageCategory category) {
        List<ServicePackage> result = new ArrayList<>();
        for (ServicePackage pkg : packages.values()) {
            if (pkg.category() == category) {
                result.add(pkg);
            }
        }
        return result;
    }
}
