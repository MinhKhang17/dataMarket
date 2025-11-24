package com.example.datasetapi.controller.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.payment.PaymentService;
import com.example.datasetapi.service.payment.TransactionService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.service.user.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/wallet")
public class WalletController {

    private final PaymentService paymentService;
    private final UserServiceImpl userServiceImpl;
    private final UserService userService;
    private final TransactionService transactionService;

    @Autowired
    public WalletController(PaymentService paymentService, UserServiceImpl userServiceImpl, UserService userService, TransactionService transactionService) {
        this.paymentService = paymentService;
        this.userServiceImpl = userServiceImpl;
        this.userService = userService;
        this.transactionService = transactionService;
    }
//    @PreAuthorize("hasRole(USER)")
//    @PostMapping
//    public ResponseEntity<?> createWallet(HttpServletRequest request) {
//        return paymentService.createWallet(request);
//    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyWallet(HttpServletRequest request) {
        return  userService.getWalletAmountFromToken(request);
    }

    @GetMapping("/transactions-history")
    public ResponseEntity<ApiResponse> getTransactions() {
        return transactionService.getTransactions();
    }


}
