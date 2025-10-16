package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.service.user.TokenServiceImpl;
import com.example.datasetapi.service.user.UserServiceImpl;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final JwtUtil jwtUtil;
    private final TokenServiceImpl tokenServiceImpl;
    private final UserServiceImpl userService;
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;


    @Autowired
    public PaymentServiceImpl(UserServiceImpl userService, WalletRepository walletRepository, JwtUtil jwtUtil, TokenServiceImpl tokenServiceImpl, TransactionService transactionService) {
        this.userService = userService;
        this.walletRepository = walletRepository;
        this.jwtUtil = jwtUtil;
        this.tokenServiceImpl = tokenServiceImpl;
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<ApiResponse> createWallet(HttpServletRequest request) {
        Wallet wallet = new Wallet();

        String token = tokenServiceImpl.resolveToken(request);
        if(token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.INVALID_TOKEN);
        }
        User user = userService.findUserById(userId);

        wallet.setUser(user);

        walletRepository.save(wallet);
return ResponseEntity.ok().body(new ApiResponse(true,"created wallet for" + userId,null));
    }

    @Override
    public boolean updateWallet(TransferType type, long amount, long user_id) {
        boolean isUpdateSuccess = false;
        switch (type){
            case TOUP:
                isUpdateSuccess = updateToUp(amount,user_id,type);
                break;
                case WITHDRAW:
                 isUpdateSuccess = updateWithdraw(amount,user_id,type);
                break;
        }
        return isUpdateSuccess;
    }

    private boolean updateWithdraw(long amount, long userId, TransferType type) {
        Optional<Wallet> wallet = walletRepository.findByUserId(userId);

        if(!wallet.isPresent()){
            System.out.println("wallet not found for user: " + userId);
            return false;
        }

        if(amount<=0){
            System.out.println("amount need greater than 0");
            return false;
        }

        //cap nhat wallet
        if(wallet.get().getAmount() < amount){
            System.out.println("amount not enough. Current balance: " + wallet.get().getAmount() + ", requested: " + amount);
            return false;
        }

        wallet.get().setAmount(wallet.get().getAmount()-amount);
        //luu vao transaction
         transactionService.createTransaction(type,amount,userId,wallet.get());
        //save vao repo
        walletRepository.save(wallet.get());

        System.out.println("Successfully withdrawn " + amount + " from user " + userId + ". New balance: " + wallet.get().getAmount());
        return true;
    }

    private boolean updateToUp(long amount, long userId, TransferType type) {
        Optional<Wallet> wallet = walletRepository.findByUserId(userId);
        if(!wallet.isPresent()){
            System.out.println("wallet not found for user: " + userId);
            return false;
        }
        if(amount<=0){
            System.out.println("amount need greater than 0");
            return false;
        }

        long oldAmount = wallet.get().getAmount();
        wallet.get().setAmount(wallet.get().getAmount()+amount);
        transactionService.createTransaction(type,amount,userId,wallet.get());
        walletRepository.save(wallet.get());

        System.out.println("Successfully added " + amount + " to user " + userId + ". Old balance: " + oldAmount + ", New balance: " + wallet.get().getAmount());
        return true;
    }
}
