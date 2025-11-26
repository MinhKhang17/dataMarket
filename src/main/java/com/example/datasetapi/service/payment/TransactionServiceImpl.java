package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.TransactionResponse;
import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.Transaction;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.TransactionRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private TransactionRepository transactionRepository;
    private final TokenService tokenService;
    private final UserService userService;
    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;
    private final WalletService walletService;


    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, TokenService tokenService, UserService userService, HttpServletRequest request, JwtUtil jwtUtil, WalletService walletService) {
        this.transactionRepository = transactionRepository;
        this.tokenService = tokenService;
        this.userService = userService;
        this.request = request;
        this.jwtUtil = jwtUtil;
        this.walletService = walletService;
    }

    @Override
    public Transaction createTransaction(TransferType transferType, double amount, long user_id, Wallet wallet, BuyType buyType) {

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setWallet(wallet);
        transaction.setType(transferType);
        transaction.setBuyType(buyType);
        return transactionRepository.save(transaction);
    }

    @Override
    public ResponseEntity<ApiResponse> getTransactions() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Wallet wallet = walletService.findWalletByUserId(userId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.WALLET_NOT_FOUND));

        List<Transaction> transactions = transactionRepository.findByWalletOrderByCreatedAtDesc(wallet.getId());
        List<TransactionResponse> response = transactions.stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getType().name(),
                        t.getAmount(),
                        t.getBuyType().name(),
                        t.getCreatedAt().toString()
                )).toList();


        return ResponseEntity.ok(new ApiResponse(true, "Transactions retrieved successfully", response));
    }

    @Override
    public List<Transaction> findByBuyType(String buyType) {
        return transactionRepository.findByBuyType(BuyType.valueOf(buyType));
    }
}
