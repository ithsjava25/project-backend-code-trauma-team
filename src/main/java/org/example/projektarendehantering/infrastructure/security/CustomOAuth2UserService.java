package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.common.Role;
import org.example.projektarendehantering.infrastructure.persistence.AccountEntity;
import org.example.projektarendehantering.infrastructure.persistence.AccountRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccountRepository accountRepository;

    public CustomOAuth2UserService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String login = oAuth2User.getAttribute("login");
        String fullName = oAuth2User.getAttribute("name");

        if (login != null) {
            UUID userId = UUID.nameUUIDFromBytes(login.getBytes(StandardCharsets.UTF_8));
            
            // Check if account already exists
            accountRepository.findById(userId).ifPresentOrElse(
                account -> {
                    // Update display name if it changed on GitHub
                    if (fullName != null && !fullName.equals(account.getDisplayName())) {
                        account.setDisplayName(fullName);
                        accountRepository.save(account);
                    }
                },
                () -> {
                    // FIRST LOGIN: Create central account with PENDING role
                    AccountEntity newAccount = new AccountEntity(
                        userId,
                        login,
                        fullName != null ? fullName : login,
                        Role.PENDING,
                        Instant.now()
                    );
                    accountRepository.save(newAccount);
                }
            );
        }

        return oAuth2User;
    }
}
