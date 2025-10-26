package com.example.datasetapi.controller.adminController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.service.user.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("providerRegis/getPending")
    public ResponseEntity<?> getProviderRegisPending() {
        return ResponseEntity.ok().body(new ApiResponse(true, "Provider Pending status load success", adminService.getProviderRegisPending(RegistrationStatus.PENDING)));
    }

    @PostMapping("providerRegis/accept")
    public ResponseEntity<?> acceptProviderRegis(@RequestParam("providerRegistrationId") long providerRegistrationId, HttpServletRequest request) {
        return adminService.acceptProviderRegis(providerRegistrationId, request);
    }

    @PostMapping("providerRegis/reject")
    public ResponseEntity<?> rejectProviderRegis(@RequestParam("providerRegistrationId") long providerRegistrationId, @RequestParam("reason") String reason, HttpServletRequest request) {
    return adminService.rejectProviderRegis(providerRegistrationId,reason,request);
    }

    @GetMapping("providerRegis/getReview")
    public ResponseEntity<?> getProviderReview() {
        return adminService.getReviewProviderHistory();
    }
    @GetMapping("providerRegis/get")
    public ResponseEntity<?> getProviderRegisAcc() {
        return ResponseEntity.ok().body(new ApiResponse(true, "Provider Pending status load success", adminService.getProviderRegisPending(RegistrationStatus.APPROVED)));
    }
}
