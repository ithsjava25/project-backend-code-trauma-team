package org.example.projektarendehantering.shared;

import java.util.UUID;

public record Actor(UUID userId, Role role, String displayName, String githubUsername) {


}
