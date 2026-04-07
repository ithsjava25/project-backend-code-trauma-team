package org.example.projektarendehantering.common;

/**
 * Actor authorization role.
 * <p>
 * Note: enum constant names are intended to be stable because infrastructure may parse them from headers.
 */
public enum Role {
    MANAGER,
    DOCTOR,
    NURSE,
    PATIENT
}
