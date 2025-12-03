package com.example.usermanagementservice.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Fetch ALL users of a company including admins + super admins
    List<User> findByCompany_Id(Long companyId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.company.id = :companyId AND r.name = 'ROLE_EMPLOYEE'")
    List<User> findEmployeesByCompanyId(@Param("companyId") Long companyId);
}
