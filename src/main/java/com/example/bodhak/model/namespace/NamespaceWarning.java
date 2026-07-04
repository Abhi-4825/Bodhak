package com.example.bodhak.model.namespace;

/**
 * Language-neutral namespace-level warning types.
 * Java:   applies to packages.
 * Python: applies to modules/directories.
 */
public enum NamespaceWarning {
    /** No namespace declared (default package / no __init__.py). */
    DEFAULT_NAMESPACE,
    /** Namespace has too many incoming + outgoing dependencies. */
    HIGH_COUPLING,
    /** Namespace contains too many entities (god package). */
    GOD_NAMESPACE,
    /** Namespace is used by many others but depends on almost nothing (hub). */
    HUB_NAMESPACE
}
