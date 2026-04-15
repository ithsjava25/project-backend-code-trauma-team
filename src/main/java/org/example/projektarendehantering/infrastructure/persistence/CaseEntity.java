package org.example.projektarendehantering.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cases")
public class CaseEntity {

    @Id
    private UUID id;
    private String status;
    private UUID ownerId;
    private String title;
    private String description;
    private Instant createdAt;
    private UUID handlerId;
    private UUID otherId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "patient_id", nullable = true) // Optional because the patient can be null
    private PatientEntity patient;

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<CaseNoteEntity> notes = new ArrayList<>();

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("uploadedAt DESC")
    @Builder.Default
    private List<DocumentEntity> documents = new ArrayList<>();

}
