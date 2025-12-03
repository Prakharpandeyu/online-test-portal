package com.example.auth.user;

import com.example.auth.integration.UserClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserClient userClient;

    public CustomUserDetailsService(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        var userDto = userClient.getUserByEmail(principal);
        if (userDto == null) {
            throw new UsernameNotFoundException("User not found in User Management Service");
        }

        Set<SimpleGrantedAuthority> authorities = userDto.roles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                userDto.email(),
                userDto.password(),
                userDto.enabled(),
                true,
                true,
                true,
                authorities
        );
    }
}
