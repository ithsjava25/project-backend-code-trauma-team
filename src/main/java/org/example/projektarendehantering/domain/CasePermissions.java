package org.example.projektarendehantering.domain;

import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.common.NotAuthorizedException;
import org.example.projektarendehantering.common.Role;

import java.util.Objects;

/**
 * Domain-level permissions for case lifecycle actions.
 */
public final class CasePermissions {

    private CasePermissions() {
    }

    public static void assertCanCreate(Actor actor) {
        Objects.requireNonNull(actor, "actor");

        Role role = actor.role();
        if (role == Role.CASE_OWNER || role == Role.HANDLER || role == Role.ADMIN) {
            return;
        }

        throw new NotAuthorizedException("Actor is not authorized to create a case");
    }
}

