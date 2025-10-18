package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.Datasets.BuyType;
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
    public boolean updateWallet(TransferType type, double amount, long user_id, BuyType buyType) {
        boolean isUpdateSuccess = false;
        switch (type){
            case TOUP:
                isUpdateSuccess = updateToUp(amount,user_id,type);
                break;
                case WITHDRAW:
                 isUpdateSuccess = updateWithdraw(amount,user_id,type);
                break;
            case TODOWN:
                isUpdateSuccess = updateToDown(amount,user_id,type,buyType);
                break;
        }
        return isUpdateSuccess;
    }

    private boolean updateToDown(double amount, long userId, TransferType type, BuyType buyType) {
        Optional<Wallet> wallet = walletRepository.findByUserId(userId);

        if(!wallet.isPresent()){
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND);
        }

        if(amount<=0){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        //cap nhat wallet
        if(wallet.get().getAmount() < amount){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        wallet.get().setAmount(wallet.get().getAmount()-amount);
        transactionService.createTransaction(type,amount,userId,wallet.get(),buyType);
        walletRepository.save(wallet.get());
        return true;
    }

    @Override
    public double calRemainingAmount(double price, User consumer) {
        Double remaining_amount = 0.0;

        double consumer_amount = walletRepository.findByUserId(consumer.getId())
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND))
                .getAmount();

        remaining_amount = consumer_amount - price;
        return remaining_amount;
    }

    private boolean updateWithdraw(double amount, long userId, TransferType type) {
        Optional<Wallet> wallet = walletRepository.findByUserId(userId);

        if(!wallet.isPresent()){
        throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND);
        }

        if(amount<=0){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        //cap nhat wallet
        if(wallet.get().getAmount() < amount){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        wallet.get().setAmount(wallet.get().getAmount()-amount);
        //luu vao transaction
         transactionService.createTransaction(type,amount,userId,wallet.get(), BuyType.OTHER);
        //save vao repo
        walletRepository.save(wallet.get());

        System.out.println("Successfully withdrawn " + amount + " from user " + userId + ". New balance: " + wallet.get().getAmount());
        return true;
    }

    private boolean updateToUp(double amount, long userId, TransferType type) {
        Optional<Wallet> wallet = walletRepository.findByUserId(userId);
        if(!wallet.isPresent()){
            throw new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND);
        }
        if(amount<=0){
            throw new  CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        double oldAmount = wallet.get().getAmount();


        wallet.get().setAmount(wallet.get().getAmount()+amount);
        transactionService.createTransaction(type,amount,userId,wallet.get(), BuyType.OTHER);
        walletRepository.save(wallet.get());

        System.out.println("Successfully added " + amount + " to user " + userId + ". Old balance: " + oldAmount + ", New balance: " + wallet.get().getAmount());
        return true;
    }
}
