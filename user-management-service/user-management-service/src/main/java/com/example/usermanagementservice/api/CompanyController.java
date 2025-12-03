package com.example.usermanagementservice.api;

import com.example.usermanagementservice.api.dto.CompanyRegistrationRequest;
import com.example.usermanagementservice.api.dto.CompanyRegistrationResponse;
import com.example.usermanagementservice.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;
    public CompanyController(CompanyService companyService){ this.companyService=companyService; }

    // public (gateway may allow)
    @PostMapping("/register")
    public ResponseEntity<CompanyRegistrationResponse> register(@Valid @RequestBody CompanyRegistrationRequest request){
        return ResponseEntity.ok(companyService.registerCompany(request));
    }
}
