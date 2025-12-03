package com.example.usermanagementservice.user;


import com.example.usermanagementservice.company.Company;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true) private String username;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String password;
    @Column(nullable=false) private boolean enabled = true;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    @Column(name="first_name", nullable=false, length=100) private String firstName;
    @Column(name="last_name",  nullable=false, length=100) private String lastName;
    @Column(name="date_of_birth") private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING) @Column(name="gender", length=16)
    private Gender gender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="user_roles",
            joinColumns=@JoinColumn(name="user_id"),
            inverseJoinColumns=@JoinColumn(name="role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="company_id", nullable=false)
    private Company company;

    public Long getId(){ return id; }
    public String getUsername(){ return username; }
    public void setUsername(String v){ this.username=v; }
    public String getEmail(){ return email; }
    public void setEmail(String v){ this.email=v; }
    public String getPassword(){ return password; }
    public void setPassword(String v){ this.password=v; }
    public boolean isEnabled(){ return enabled; }
    public void setEnabled(boolean v){ this.enabled=v; }
    public Instant getCreatedAt(){ return createdAt; }
    public Set<Role> getRoles(){ return roles; }
    public void setRoles(Set<Role> roles){ this.roles=roles; }
    public Company getCompany(){ return company; }
    public void setCompany(Company company){ this.company=company; }
    public String getFirstName(){ return firstName; }
    public void setFirstName(String v){ this.firstName=v; }
    public String getLastName(){ return lastName; }
    public void setLastName(String v){ this.lastName=v; }
    public LocalDate getDateOfBirth(){ return dateOfBirth; }
    public void setDateOfBirth(LocalDate v){ this.dateOfBirth=v; }
    public Gender getGender(){ return gender; }
    public void setGender(Gender v){ this.gender=v; }

    public Long getCompanyId(){ return company!=null ? company.getId() : null; }
    public String getName(){ return ((firstName!=null)?firstName:"")+" "+((lastName!=null)?lastName:""); }
}

