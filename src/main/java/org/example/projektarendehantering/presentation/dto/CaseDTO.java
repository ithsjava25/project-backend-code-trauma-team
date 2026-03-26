package org.example.projektarendehantering.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public class CaseDTO {

    private UUID id;
    private String status;
    private String title;
    private String description;
    private Instant createdAt;

    public CaseDTO() {}

    public CaseDTO(UUID id, String status, String title, String description, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
