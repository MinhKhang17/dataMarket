package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.UserRepository;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final WalletRepository walletRepository;

    @Override
    public ResponseEntity<ApiResponse> createWallet(User user) {
        if (user == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        long userId = user.getId();

        if (!userRepository.existsById(userId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND);
        }

        if (walletRepository.findByUserId(userId).isPresent()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Wallet already exists for user " + userId, null)
            );
        }

        // Tạo mới wallet
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAmount(0.0);
        wallet.setHoldBalance(0.0);

        walletRepository.save(wallet);

        return ResponseEntity.ok(
                new ApiResponse(true, "Wallet created successfully for user " + userId, null)
        );
    }

    @Override
    public Optional<Wallet> findWalletByUserId(Long id) {
        return walletRepository.findByUserId(id);
    }
}
