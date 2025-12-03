package com.example.usermanagementservice.api.dto;
public class CompanyRegistrationResponse {
    private final Long companyId;
    private final Long superAdminUserId;
    private final String message;

    public CompanyRegistrationResponse(Long companyId, Long superAdminUserId, String message) {
        this.companyId = companyId;
        this.superAdminUserId = superAdminUserId;
        this.message = message;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getSuperAdminUserId() {
        return superAdminUserId;
    }

    public String getMessage() {
        return message;
    }
}
