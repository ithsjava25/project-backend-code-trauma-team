package org.example.projektarendehantering.application.usecase;

import java.util.Objects;

public record CreateCaseCommand(String title, String description) {

    public CreateCaseCommand {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(description, "description");
    }
}

