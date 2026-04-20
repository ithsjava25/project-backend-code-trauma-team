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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String emailToken = toPiiSafeToken(normalizedEmail);
        log.debug("Loading user by email token: {}", emailToken);

        return userAccountRepository.findByEmail(normalizedEmail)
                .map(userAccount -> {
                    log.debug("Found user for token {}: role={}, enabled={}",
                            emailToken,
                            userAccount.getRole(),
                            userAccount.isEnabled());

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
                    log.error("User not found for email token: {}", emailToken);
                    return new UsernameNotFoundException("User not found");
                });
    }

    private String toPiiSafeToken(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 12);
        } catch (NoSuchAlgorithmException ex) {
            log.warn("Falling back to non-cryptographic token generation");
            return Integer.toHexString(value.hashCode());
        }
    }
}
