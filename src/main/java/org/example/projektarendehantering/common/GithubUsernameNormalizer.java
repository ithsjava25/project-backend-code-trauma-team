package org.example.projektarendehantering.common;

import java.util.Locale;

public final class GithubUsernameNormalizer {

    private GithubUsernameNormalizer() {
    }

    public static String normalize(String githubUsername) {
        if (githubUsername == null) {
            return null;
        }
        String trimmed = githubUsername.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
