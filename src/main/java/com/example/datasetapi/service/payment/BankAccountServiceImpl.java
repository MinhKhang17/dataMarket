package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.BankRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.BankResponse;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.BankAccount;
import com.example.datasetapi.repository.BankAccountRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements  BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TokenService tokenService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    @Override
    public BankAccount getBankAccount(Long id) {
        return bankAccountRepository.findById(id).get();
    }

    @Override
    public ResponseEntity<ApiResponse> addBankAccount(BankRequest bankRequest) {
        String token = tokenService.resolveToken(request);
        if(token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.INVALID_TOKEN);
        }

        if(bankRequest.getBankName() == null || bankRequest.getBankName().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if(bankRequest.getAccountNumber() == null || bankRequest.getAccountNumber().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if(bankRequest.getAccountHolderName() == null || bankRequest.getAccountHolderName().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName(bankRequest.getBankName());
        bankAccount.setAccountNumber(bankRequest.getAccountNumber());
        bankAccount.setAccountHolderName(bankRequest.getAccountHolderName());
        bankAccount.setUser(userService.findUserById(userId));
        bankAccountRepository.save(bankAccount);

        return ResponseEntity.ok().body(new ApiResponse(true, "Bank account added successfully",
                new BankResponse(bankAccount.getId(),bankAccount.getBankName(), bankAccount.getAccountNumber(), bankAccount.getAccountHolderName())));
    }

    @Override
    public List<BankAccount> getBankByUserId(Long userId) {
        return bankAccountRepository.findBankAccountByUserId(userId);
    }

    @Override
    public BankAccount addBankAccount(BankRequest bankRequest, Long userId) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName(bankRequest.getBankName());
        bankAccount.setAccountNumber(bankRequest.getAccountNumber());
        bankAccount.setAccountHolderName(bankRequest.getAccountHolderName());
        bankAccount.setUser(userService.findUserById(userId));
        bankAccountRepository.save(bankAccount);
        return bankAccount;
    }

    @Override
    public ResponseEntity<ApiResponse> listbank() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        List<BankAccount> bankAccounts = getBankByUserId(userId);
        if (bankAccounts.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.BANK_ACCOUNT_NOT_FOUND);
        }
        List<BankResponse> responses = new ArrayList<>();
         responses = bankAccounts.stream()
                .map(bank -> new BankResponse(
                        bank.getId(),
                        bank.getBankName(),
                        bank.getAccountNumber(),
                        bank.getAccountHolderName()
                ))
                .toList();

        return ResponseEntity.ok().body(new ApiResponse(true, "Bank accounts retrieved successfully", responses));

    }

    @Override
    public ResponseEntity<ApiResponse> getBankById(Long id) {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        if (!bankAccount.getUser().getId().equals(userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.NOT_OWNER_BANK_ACCOUNT);
        }
        return ResponseEntity.ok().body(new ApiResponse(true, "Bank account retrieved successfully", new BankResponse(
                bankAccount.getId(),
                bankAccount.getBankName(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountHolderName()))

        );
    }
}
