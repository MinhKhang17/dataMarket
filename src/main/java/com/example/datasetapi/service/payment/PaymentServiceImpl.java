package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.User;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final JwtUtil jwtUtil;
    private final TokenServiceImpl tokenServiceImpl;
    private UserServiceImpl userService;
    private WalletRepository walletRepository;
    private TransactionService transactionService;
    @Autowired
    public PaymentServiceImpl(UserServiceImpl userService, WalletRepository walletRepository, JwtUtil jwtUtil, TokenServiceImpl tokenServiceImpl,TransactionService transactionService) {
        this.userService = userService;
        this.walletRepository = walletRepository;
        this.jwtUtil = jwtUtil;
        this.tokenServiceImpl = tokenServiceImpl;
    }

    @Override
    public ResponseEntity<ApiResponse> createWallet(HttpServletRequest request) {
        Wallet wallet = new Wallet();

        long userId = jwtUtil.getUserIdFromToken(tokenServiceImpl.resolveToken(request));

        Optional<User> user = userService.findUserById(userId);

        if(!user.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        wallet.setUser(user.get());

        walletRepository.save(wallet);
return ResponseEntity.ok().body(new ApiResponse(true,"created wallet for" + userId,null));
    }

    @Override
    public boolean updateWallet(TransferType type, long amount, long user_id) {
//thay doi khi viet xong vertify
        boolean isCheckedUser = true;
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

        Optional<Wallet> wallet = walletRepository.findById(userId);
        if(!wallet.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found");
        }
        //cap nhat wallet
        wallet.get().setAmount(wallet.get().getAmount()-amount);
        //luu vao transaction
        boolean success =  transactionService.createTransaction(type,amount,userId,wallet.get());
        //save vao repo
success =    walletRepository.save(wallet.get())!=null;
        return success;
    }

    private boolean updateToUp(long amount, long userId, TransferType type) {

        return false;
    }
}
