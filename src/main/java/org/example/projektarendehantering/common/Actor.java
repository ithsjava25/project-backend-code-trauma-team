package org.example.projektarendehantering.common;

import java.util.UUID;

public record Actor(UUID userId, Role role, String displayName, String githubUsername) {


}
