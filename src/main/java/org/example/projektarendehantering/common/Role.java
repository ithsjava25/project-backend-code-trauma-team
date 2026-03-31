package org.example.projektarendehantering.common;

/**
 * Actor authorization role.
 * <p>
 * Note: enum constant names are intended to be stable because infrastructure may parse them from headers.
 */
public enum Role {
    /**
     * New naming (preferred).
     */
    MANAGER,
    DOCTOR,
    NURSE,
    PATIENT,

    /**
     * Legacy naming (kept for backward compatibility with header parsing).
     */
    CASE_OWNER,
    HANDLER,
    ADMIN,
    OTHER
}
