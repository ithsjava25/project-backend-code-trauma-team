package org.example.projektarendehantering.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDTO {

    private UUID id;
    private String status;
    private String title;
    private String description;
    private Instant createdAt;
    private UUID patientId;

    @Builder.Default
    private List<CaseNoteDTO> notes = new ArrayList<>();

    @Builder.Default
    private List<DocumentDTO> documents = new ArrayList<>();

}
