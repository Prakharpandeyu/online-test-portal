package com.example.usermanagementservice.company;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "companies")
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name="gst_number", nullable = false, length = 32, unique = true)
    private String gstNumber;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Company() {}
    public Company(String name, String gstNumber){ this.name=name; this.gstNumber=gstNumber; }

    @PrePersist public void prePersist(){ if (createdAt==null) createdAt = Instant.now(); }

    public Long getId(){ return id; }
    public String getName(){ return name; }
    public String getGstNumber(){ return gstNumber; }
    public Instant getCreatedAt(){ return createdAt; }
    public void setName(String n){ this.name=n; }
    public void setGstNumber(String g){ this.gstNumber=g; }
}
