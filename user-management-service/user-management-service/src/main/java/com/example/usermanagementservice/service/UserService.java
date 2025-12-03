package com.example.usermanagementservice.service;

import com.example.usermanagementservice.api.dto.CreateUserRequest;
import com.example.usermanagementservice.api.dto.UpdateUserProfileRequest;
import com.example.usermanagementservice.company.Company;
import com.example.usermanagementservice.user.Role;
import com.example.usermanagementservice.user.RoleRepository;
import com.example.usermanagementservice.user.User;
import com.example.usermanagementservice.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // CREATE USER (existing)
    @Transactional
    public User createUser(CreateUserRequest request, String roleName, Company company) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already exists");

        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email already exists");

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setCompany(company);
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }

    // UPDATE OWN PROFILE
    @Transactional
    public User updateProfile(Long userId, UpdateUserProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update username
        if (req.getUsername() != null) {
            String newUsername = req.getUsername().trim();

            if (!newUsername.equals(user.getUsername()) &&
                    userRepository.existsByUsername(newUsername)) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(newUsername);
        }

        // Update email
        if (req.getEmail() != null) {
            String newEmail = req.getEmail().trim().toLowerCase();

            if (!newEmail.equals(user.getEmail()) &&
                    userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(newEmail);
        }

        if (req.getFirstName() != null)
            user.setFirstName(req.getFirstName().trim());

        if (req.getLastName() != null)
            user.setLastName(req.getLastName().trim());

        if (req.getDateOfBirth() != null)
            user.setDateOfBirth(req.getDateOfBirth());

        if (req.getGender() != null)
            user.setGender(req.getGender());

        return userRepository.save(user);
    }

    // UPDATE EMPLOYEE (ADMIN / SUPER ADMIN)
    @Transactional
    public User updateEmployeeProfile(Long targetUserId,
                                      Long callerCompanyId,
                                      String callerRole,
                                      UpdateUserProfileRequest req) {

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Must belong to same company
        if (!target.getCompanyId().equals(callerCompanyId)) {
            throw new IllegalArgumentException("You cannot edit another company's employee");
        }

        // ADMIN cannot edit Admin or Super Admin
        if (callerRole.equals("ADMIN")) {
            boolean higherRole = target.getRoles().stream().anyMatch(r ->
                    r.getName().equals("ROLE_ADMIN") ||
                            r.getName().equals("ROLE_SUPER_ADMIN")
            );

            if (higherRole) {
                throw new IllegalArgumentException("Admins cannot edit admins or super admins");
            }
        }

        // Update username
        if (req.getUsername() != null) {
            String newUsername = req.getUsername().trim();
            if (!newUsername.equals(target.getUsername()) &&
                    userRepository.existsByUsername(newUsername)) {
                throw new IllegalArgumentException("Username already exists");
            }
            target.setUsername(newUsername);
        }

        // Update email
        if (req.getEmail() != null) {
            String newEmail = req.getEmail().trim().toLowerCase();
            if (!newEmail.equals(target.getEmail()) &&
                    userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            target.setEmail(newEmail);
        }

        if (req.getFirstName() != null)
            target.setFirstName(req.getFirstName().trim());

        if (req.getLastName() != null)
            target.setLastName(req.getLastName().trim());

        if (req.getDateOfBirth() != null)
            target.setDateOfBirth(req.getDateOfBirth());

        if (req.getGender() != null)
            target.setGender(req.getGender());

        return userRepository.save(target);
    }

    // CHANGE OWN PASSWORD
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    // RESET EMPLOYEE PASSWORD (Admin / Super Admin)
    @Transactional
    public void resetPassword(Long targetUserId, Long callerCompanyId, String callerRole, String newPassword) {

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Must belong to same company
        if (!target.getCompanyId().equals(callerCompanyId)) {
            throw new IllegalArgumentException("You cannot reset password of another company's user");
        }

        // ADMIN cannot reset Admin or Super Admin passwords
        if (callerRole.equals("ADMIN")) {
            boolean targetIsAdminOrSuper = target.getRoles().stream().anyMatch(r ->
                    r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_SUPER_ADMIN")
            );

            if (targetIsAdminOrSuper) {
                throw new IllegalArgumentException("Admins cannot reset password of admins or super admins");
            }
        }

        // SUPER ADMIN can reset anyone in their company
        target.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(target);
    }
}
