package com.example.datasetapi.controller.adminController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.user.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @GetMapping("providerRegis/get")
    public ResponseEntity<?> getProviderRegis()
    {
            return ResponseEntity.ok().body(new ApiResponse(true,"Provider Pending status load success",adminService.getProviderRegisPending()));
    }
}
