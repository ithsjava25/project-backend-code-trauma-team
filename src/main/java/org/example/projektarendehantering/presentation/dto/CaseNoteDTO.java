package org.example.projektarendehantering.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseNoteDTO {

    private UUID id;
    private String content;
    private String authorDisplayName;
    private String authorGithubUsername;
    private String authorRole;
    private Instant createdAt;

}
