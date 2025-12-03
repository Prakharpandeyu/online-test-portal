package com.example.usermanagementservice.config;

import com.example.usermanagementservice.user.Role;
import com.example.usermanagementservice.user.RoleRepository;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final RoleRepository roleRepository;
    public DataInitializer(RoleRepository roleRepository){ this.roleRepository=roleRepository; }

    @Override @Transactional
    public void run(String... args){
        ensureRole("ROLE_SUPER_ADMIN");
        ensureRole("ROLE_ADMIN");
        ensureRole("ROLE_EMPLOYEE");
    }
    private void ensureRole(String name){
        roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name)));
    }
}

