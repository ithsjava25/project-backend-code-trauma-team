package org.example.projektarendehantering.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projektarendehantering.infrastructure.persistence.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        return userAccountRepository.findByEmail(email)
                .map(userAccount -> {
                    log.debug("Found user: email={}, role={}, enabled={}",
                            userAccount.getEmail(),
                            userAccount.getRole(),
                            userAccount.isEnabled());
                    log.debug("Password hash from DB: {}", userAccount.getPasswordHash());

                    return new User(
                            userAccount.getEmail(),
                            userAccount.getPasswordHash(),
                            userAccount.isEnabled(),
                            true,
                            true,
                            true,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userAccount.getRole().name()))
                    );
                })
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }
}
