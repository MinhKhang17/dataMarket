package com.example.datasetapi.service.payment;

import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.service.user.TokenServiceImpl;
import com.example.datasetapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;


    @Autowired
    public PaymentServiceImpl(WalletRepository walletRepository, JwtUtil jwtUtil, TokenServiceImpl tokenServiceImpl, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    @Override
    public boolean updateWallet(TransferType type, double amount, long user_id, BuyType buyType) {
        boolean isUpdateSuccess = false;
        switch (type){
            case TOPUP:
                isUpdateSuccess = topUp(amount,user_id,type);
                break;
            case WITHDRAW_HOLD:
                isUpdateSuccess = holdWithdraw(amount,user_id,type);
                break;
            case WITHDRAW_APPROVE:
                isUpdateSuccess = approveWithdraw(amount, user_id, type);
                break;

            case WITHDRAW_REJECT:
                isUpdateSuccess = rejectWithdraw(amount, user_id, type);
                break;
            case PAYOUT:
                isUpdateSuccess = payOut(amount,user_id,type,buyType);
                break;
        }
        return isUpdateSuccess;
    }

    @Transactional
    protected boolean rejectWithdraw(double amount, long userId, TransferType type) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

        if(amount<=0){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.INVALID_AMOUNT);
        }

        if(wallet.getHoldBalance() < amount){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        wallet.setHoldBalance(wallet.getHoldBalance() - amount);
        wallet.setAmount(wallet.getAmount() + amount);

        //luu vao transaction
        transactionService.createTransaction(type,amount,userId,wallet, BuyType.WITHDRAW);
        //save vao repo
        walletRepository.save(wallet);

        System.out.println("Successfully refund " + amount + " from user " + userId + ". New balance: " + wallet.getAmount());
        return true;
    }

    @Transactional
    protected boolean approveWithdraw(double amount, long userId, TransferType type) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

        if(amount<=0){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.INVALID_AMOUNT);
        }

        if(wallet.getHoldBalance() < amount){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        wallet.setHoldBalance(wallet.getHoldBalance() - amount);
        //luu vao transaction
        transactionService.createTransaction(type,amount,userId,wallet, BuyType.WITHDRAW);
        //save vao repo
        walletRepository.save(wallet);

        System.out.println("Successfully withdraw " + amount + " from user " + userId + ". New balance: " + wallet.getAmount());
        return true;
    }

    @Transactional
    protected boolean payOut(double amount, long userId, TransferType type, BuyType buyType) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

        if(amount<=0){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.INVALID_AMOUNT);
        }

        //cap nhat wallet
        if(wallet.getAmount() < amount){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        wallet.setAmount(wallet.getAmount()-amount);
        transactionService.createTransaction(type,amount,userId,wallet,buyType);
        walletRepository.save(wallet);
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


    @Transactional
    protected boolean holdWithdraw(double amount, long userId, TransferType type) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

        if(amount<=0){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.INVALID_AMOUNT);
        }

        if(wallet.getAmount() < amount){
            throw new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        wallet.setAmount(wallet.getAmount() - amount);
        wallet.setHoldBalance(wallet.getHoldBalance() + amount);
        //luu vao transaction
        transactionService.createTransaction(type,amount,userId,wallet, BuyType.WITHDRAW);
        //save vao repo
        walletRepository.save(wallet);

        System.out.println("Successfully withdraw " + amount + " from user " + userId + ". New balance: " + wallet.getAmount());
        return true;
    }

    @Transactional
    protected boolean topUp(double amount, long userId, TransferType type) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

        if(amount<=0){
            throw new  CustomException(HttpStatus.BAD_REQUEST,ErrorCode.AMOUNT_NOT_ENOUGH);
        }

        double oldAmount = wallet.getAmount();

        wallet.setAmount(wallet.getAmount() + amount);
        transactionService.createTransaction(type,amount,userId,wallet, BuyType.TOP_UP);
        walletRepository.save(wallet);

        System.out.println("Successfully added " + amount + " to user " + userId + ". Old balance: " + oldAmount + ", New balance: " + wallet.getAmount());
        return true;
    }
}
