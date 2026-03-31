package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cases")
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String status;
    private UUID ownerId;
    private String title;
    private String description;
    private Instant createdAt;

    @ManyToOne(optional = true)
    @JoinColumn(name = "patient_id", nullable = true) // Optional because the patient can be null
    private PatientEntity patient;

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<CaseNoteEntity> notes = new ArrayList<>();

    public CaseEntity() {}

    public CaseEntity(UUID id, String status, UUID ownerId, String title, String description, Instant createdAt, PatientEntity patient, List<CaseNoteEntity> notes) {
        this.id = id;
        this.status = status;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.patient = patient;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public PatientEntity getPatient() { return patient; }
    public void setPatient(PatientEntity patient) { this.patient = patient; }

    public List<CaseNoteEntity> getNotes() { return notes; }

}
