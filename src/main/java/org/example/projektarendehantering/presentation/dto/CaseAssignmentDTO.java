package org.example.projektarendehantering.presentation.dto;

import java.util.UUID;

public class CaseAssignmentDTO {

    private UUID ownerId;
    private UUID handlerId;
    private UUID otherId;

    public CaseAssignmentDTO() {}

    public CaseAssignmentDTO(UUID ownerId, UUID handlerId, UUID otherId) {
        this.ownerId = ownerId;
        this.handlerId = handlerId;
        this.otherId = otherId;
    }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public UUID getHandlerId() { return handlerId; }
    public void setHandlerId(UUID handlerId) { this.handlerId = handlerId; }

    public UUID getOtherId() { return otherId; }
    public void setOtherId(UUID otherId) { this.otherId = otherId; }
}

