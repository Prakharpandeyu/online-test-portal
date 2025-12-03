package com.example.usermanagementservice.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyRegistrationRequest {
    @NotBlank
    @Size(min = 2, max = 255)
    private String companyName;

    @NotBlank
    @Size(min = 5, max = 32)
    private String gstNumber;

    @NotBlank
    @Email
    private String superAdminEmail;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @Size(min = 3, max = 50)
    private String superAdminUsername;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public String getSuperAdminEmail() { return superAdminEmail; }
    public void setSuperAdminEmail(String superAdminEmail) { this.superAdminEmail = superAdminEmail; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSuperAdminUsername() { return superAdminUsername; }
    public void setSuperAdminUsername(String superAdminUsername) { this.superAdminUsername = superAdminUsername;
    }

}