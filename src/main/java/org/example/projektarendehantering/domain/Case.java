package org.example.projektarendehantering.domain;

import org.example.projektarendehantering.common.Actor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Case aggregate root.
 */
public final class Case {

    private final CaseId id;
    private final CaseStatus status;
    private final UUID ownerId;
    private final String title;
    private final String description;
    private final Instant createdAt;

    private Case(
            CaseId id,
            CaseStatus status,
            UUID ownerId,
            String title,
            String description,
            Instant createdAt
    ) {
        this.id = id;
        this.status = status;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    /**
     * Creates a new OPEN case.
     */
    public static Case open(Actor actor, String title, String description) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(description, "description");

        CaseId id = new CaseId(UUID.randomUUID());
        return new Case(
                id,
                CaseStatus.OPEN,
                actor.userId(),
                title,
                description,
                Instant.now()
        );
    }

    /**
     * Alias used by the first vertical slice ("create case").
     */
    public static Case create(Actor actor, String title, String description) {
        return open(actor, title, description);
    }

    public CaseId id() {
        return id;
    }

    public CaseStatus status() {
        return status;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }
}

