package org.example.projektarendehantering.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public class CaseNoteDTO {

    private UUID id;
    private String content;
    private String author;
    private Instant createdAt;

    public CaseNoteDTO() {}

    public CaseNoteDTO(UUID id, String content, String author, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
