package org.example.projektarendehantering.cases.application;

import java.time.Instant;
import java.util.UUID;

public class CaseNoteDTO {

    private UUID id;
    private String content;
    private String authorDisplayName;
    private String authorGithubUsername;
    private String authorRole;
    private Instant createdAt;

    public CaseNoteDTO() {}

    public CaseNoteDTO(UUID id, String content, String authorDisplayName, String authorGithubUsername, String authorRole, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.authorDisplayName = authorDisplayName;
        this.authorGithubUsername = authorGithubUsername;
        this.authorRole = authorRole;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorDisplayName() { return authorDisplayName; }
    public void setAuthorDisplayName(String authorDisplayName) { this.authorDisplayName = authorDisplayName; }

    public String getAuthorGithubUsername() { return authorGithubUsername; }
    public void setAuthorGithubUsername(String authorGithubUsername) { this.authorGithubUsername = authorGithubUsername; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
