package org.example.projektarendehantering.identity.infrastructure;

import org.example.projektarendehantering.shared.Role;
import org.example.projektarendehantering.identity.domain.AccountEntity;
import org.example.projektarendehantering.identity.domain.AccountRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
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

        Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());

        if (login != null) {
            UUID userId = UUID.nameUUIDFromBytes(login.getBytes(StandardCharsets.UTF_8));
            
            // Check if account already exists
            var accountOpt = accountRepository.findById(userId);
            
            if (accountOpt.isPresent()) {
                AccountEntity account = accountOpt.get();
                // Update display name if it changed on GitHub
                if (fullName != null && !fullName.equals(account.getDisplayName())) {
                    account.setDisplayName(fullName);
                    accountRepository.save(account);
                }
                
                // Add the role from DB to authorities
                if (account.getRole() != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
                }
            } else {
                // FIRST LOGIN: Create central account with PENDING role
                AccountEntity newAccount = new AccountEntity(
                    userId,
                    login,
                    fullName != null ? fullName : login,
                    Role.PENDING,
                    Instant.now()
                );
                accountRepository.save(newAccount);
                authorities.add(new SimpleGrantedAuthority("ROLE_PENDING"));
            }
        }

        // Return a new user with the updated authorities
        return new DefaultOAuth2User(
            authorities, 
            oAuth2User.getAttributes(), 
            userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}
