package org.example.projektarendehantering.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAssignmentDTO {

    private UUID ownerId;
    private UUID handlerId;

}
