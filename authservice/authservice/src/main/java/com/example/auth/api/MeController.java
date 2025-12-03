package com.example.auth.api;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public ResponseEntity<Info> me(Authentication auth) {
        List<String> authorities = auth.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        return ResponseEntity.ok(new Info(auth.getName(), authorities));
    }
    public record Info(String username, List<String> authorities) {}
}