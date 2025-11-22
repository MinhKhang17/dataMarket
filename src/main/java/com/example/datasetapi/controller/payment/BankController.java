package com.example.datasetapi.controller.payment;

import com.example.datasetapi.dto.request.BankRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.service.payment.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/bank")
public class BankController {
    private final BankAccountService bankAccountService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createBankAccount(@RequestBody BankRequest bankRequest) {
        return bankAccountService.addBankAccount(bankRequest);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> listBank() {
        return bankAccountService.listbank();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBankAccount(@PathVariable Long id) {
        return bankAccountService.getBankById(id);
    }
}
