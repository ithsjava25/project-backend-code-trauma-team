package org.example.projektarendehantering.infrastructure.security;

import org.example.projektarendehantering.infrastructure.persistence.EmployeeRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final EmployeeRepository employeeRepository;

    public CustomOAuth2UserService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = loadBaseUser(userRequest);

        String login = oAuth2User.getAttribute("login");
        Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());

        if (login != null) {
            // Check if user exists in our database by their githubUsername
            employeeRepository.findByGithubUsername(login).ifPresent(employee -> {
                if (employee.getRole() != null) {
                    // Inject the database role as a Spring Security authority (ROLE_XXX)
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
                }
            });
        }

        return new DefaultOAuth2User(
            authorities, 
            oAuth2User.getAttributes(), 
            userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    protected OAuth2User loadBaseUser(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }
}
