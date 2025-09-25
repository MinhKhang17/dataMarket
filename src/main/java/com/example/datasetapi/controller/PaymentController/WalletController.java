package com.example.datasetapi.controller.PaymentController;

import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/wallet")
public class WalletController {

    private final PaymentService paymentService;

    @Autowired
    public WalletController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @PreAuthorize("hasRole(USER)")
    @PostMapping
    public ResponseEntity<?> createWallet(HttpServletRequest request) {
        return paymentService.createWallet(request);
    }
}
