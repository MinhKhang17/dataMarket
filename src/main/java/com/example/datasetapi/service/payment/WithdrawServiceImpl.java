package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.ProcessWithdrawRequest;
import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.WithdrawResponse;
import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.model.paySystem.Withdraw;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.WithdrawRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements     WithdrawService {
    private final WithdrawRepository withdrawRepository;
    private final WalletService walletService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final UserService userService;
    private final PaymentService paymentService;

    @Override
    public ResponseEntity<ApiResponse> withdrawRequest(WithdrawRequest withdrawRequest) {
            String token = tokenService.resolveToken(request);
            if(token == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.UNAUTHORIZED);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.INVALID_TOKEN);
            }


            Wallet wallet = walletService.findWalletByUserId(userId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

            if (!wallet.getUser().getId().equals(userId)) {
                throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
            }
            Long amount = withdrawRequest.getAmount();
            if (amount == null || amount <= 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_WITHDRAW_AMOUNT);
            }
            if (wallet.getAmount() < amount) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_FUNDS);
            }

            Withdraw withdraw = new Withdraw();
            withdraw.setUser(wallet.getUser());
            withdraw.setWallet(wallet);
            withdraw.setAmount(withdrawRequest.getAmount());
            withdraw.setStatus(Withdraw.Status.PENDING);
            withdrawRepository.saveAndFlush(withdraw);

            return ResponseEntity.ok(new ApiResponse(true, "Success", new WithdrawResponse(withdraw.getId(), withdraw.getStatus().name(), withdraw.getAmount(), withdraw.getCreatedAt(), withdraw.getUpdatedAt(), wallet.getId(), null, null)));
    }

    @Override
    public ResponseEntity<ApiResponse> processWithdrawRequest(ProcessWithdrawRequest withdrawRequest) {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        Long adminId = jwtUtil.getUserIdFromToken(token);
        if (adminId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }

        User admin = userService.findUserById(adminId);
        if (!admin.getRole().getName().contains("ADMIN")) {
            throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }

        Withdraw withdraw = withdrawRepository.findById(withdrawRequest.getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.WITHDRAW_NOT_FOUND));

        if (withdraw.getStatus() != Withdraw.Status.PENDING) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.ALREADY_PROCESSED);
        }

        Withdraw.Status newStatus;
        try {
            newStatus = Withdraw.Status.valueOf(withdrawRequest.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_INPUT);
        }

        if (newStatus == Withdraw.Status.REJECT) {
            if (withdrawRequest.getReason() == null || withdrawRequest.getReason().trim().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REASON);
            }
            withdraw.setReason(withdrawRequest.getReason());
        }

        withdraw.setStatus(newStatus);
        Wallet wallet = withdraw.getWallet();

        if (newStatus == Withdraw.Status.APPROVE) {
            paymentService.updateWallet(TransferType.WITHDRAW, withdraw.getAmount(), withdraw.getUser().getId(), BuyType.OTHER);

            withdraw.setProofImageUrl(withdrawRequest.getProofImageUrl());

            if (withdrawRequest.getReason() != null && !withdrawRequest.getReason().isBlank()) {
                withdraw.setReason(withdrawRequest.getReason());
            }
        }

        withdrawRepository.saveAndFlush(withdraw);

        return ResponseEntity.ok(new ApiResponse(true, "Success", new WithdrawResponse(withdraw.getId(), withdraw.getStatus().name(), withdraw.getAmount(), withdraw.getCreatedAt(), withdraw.getUpdatedAt(), wallet.getId(), withdraw.getReason(), withdraw.getProofImageUrl())));
    }
}
