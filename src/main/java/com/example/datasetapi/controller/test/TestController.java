package com.example.datasetapi.controller.test;

import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.service.feature.S3Service;
import com.example.datasetapi.service.payment.PaymentService;
import com.example.datasetapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("test/security")
public class TestController {
    private PaymentService paymentService;

    @Autowired
   private JwtUtil jwtUtil ;
@Autowired
public TestController(PaymentService paymentService) {
    this.paymentService = paymentService;
}

    @Autowired
    private S3Service s3Service;
@PreAuthorize("hasRole(USER)")
    @GetMapping()
    public ResponseEntity<String> testToken() {
        paymentService.updateWallet(TransferType.TOUP,100,1);
    return  ResponseEntity.ok().body("success");
    }

}
