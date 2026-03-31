package org.example.projektarendehantering.presentation.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CaseDTO {

    private UUID id;
    private String status;
    private String title;
    private String description;
    private Instant createdAt;
    private UUID patientId;
    private List<CaseNoteDTO> notes = new ArrayList<>();

    public CaseDTO() {}

    public CaseDTO(UUID id, String status, String title, String description, Instant createdAt, UUID patientId) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.patientId = patientId;
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

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public List<CaseNoteDTO> getNotes() { return notes; }
    public void setNotes(List<CaseNoteDTO> notes) { this.notes = notes; }
}
