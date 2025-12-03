package com.example.usermanagementservice.service;

import com.example.usermanagementservice.api.dto.CompanyRegistrationRequest;
import com.example.usermanagementservice.api.dto.CompanyRegistrationResponse;
import com.example.usermanagementservice.api.dto.UpdateCompanyRequest;
import com.example.usermanagementservice.company.Company;
import com.example.usermanagementservice.company.CompanyRepository;
import com.example.usermanagementservice.user.Role;
import com.example.usermanagementservice.user.RoleRepository;
import com.example.usermanagementservice.user.User;
import com.example.usermanagementservice.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CompanyService(CompanyRepository companyRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // REGISTER NEW COMPANY + CREATE SUPER ADMIN

    @Transactional
    public CompanyRegistrationResponse registerCompany(CompanyRegistrationRequest req) {
        String companyName = req.getCompanyName().trim();
        String gst = req.getGstNumber().trim().toUpperCase();
        String email = req.getSuperAdminEmail().trim().toLowerCase();

        String username = (req.getSuperAdminUsername() != null && !req.getSuperAdminUsername().isBlank())
                ? req.getSuperAdminUsername().trim()
                : deriveUsernameFromEmail(email);

        if (companyRepository.existsByGstNumber(gst))
            throw new IllegalArgumentException("GST Number already registered");

        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already exists");

        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email already exists");

        Company company = companyRepository.save(new Company(companyName, gst));

        Role superRole = roleRepository.findByName("ROLE_SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN missing"));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);
        user.setCompany(company);
        user.setRoles(Set.of(superRole));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Duplicate user data", e);
        }

        return new CompanyRegistrationResponse(
                company.getId(),
                user.getId(),
                "Company registered and Super Admin created"
        );
    }

    private String deriveUsernameFromEmail(String email) {
        int at = email.indexOf('@');
        if (at > 0) {
            String local = email.substring(0, at);
            return local.replaceAll("[^A-Za-z0-9._-]", "").toLowerCase();
        }
        return email;
    }


    // UPDATE COMPANY DETAILS (SUPER ADMIN ONLY)

    @Transactional
    public void updateCompany(Long companyId, UpdateCompanyRequest req) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        String newName = req.getName().trim();
        String newGst = req.getGstNumber().trim().toUpperCase();

        // Check GST uniqueness except for same company
        companyRepository.findByGstNumber(newGst).ifPresent(existing -> {
            if (!existing.getId().equals(companyId)) {
                throw new IllegalArgumentException("GST Number already in use");
            }
        });

        company.setName(newName);
        company.setGstNumber(newGst);

        companyRepository.save(company);
    }
}
