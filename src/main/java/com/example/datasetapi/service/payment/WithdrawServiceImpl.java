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
import com.example.datasetapi.service.feature.ImageService;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {
    private final WithdrawRepository withdrawRepository;
    private final WalletService walletService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final UserService userService;
    private final PaymentService paymentService;
    private final ImageService imageService;

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
    public ResponseEntity<ApiResponse> processWithdrawRequest(ProcessWithdrawRequest withdrawRequest, MultipartFile file) throws IOException {
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

        if (newStatus == Withdraw.Status.APPROVE) {
            if (file == null || file.isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
            }

            String imageUrl = imageService.uploadImage(file);
            withdraw.setProofImageUrl(imageUrl);

            paymentService.updateWallet(
                    TransferType.WITHDRAW,
                    withdraw.getAmount(),
                    withdraw.getUser().getId(),
                    BuyType.OTHER
            );

            if (withdrawRequest.getReason() != null && !withdrawRequest.getReason().isBlank()) {
                withdraw.setReason(withdrawRequest.getReason());
            }
        }

        withdraw.setStatus(newStatus);
        withdrawRepository.saveAndFlush(withdraw);

        Wallet wallet = withdraw.getWallet();

        return ResponseEntity.ok(new ApiResponse(true, "Success",
                new WithdrawResponse(withdraw.getId(), withdraw.getStatus().name(), withdraw.getAmount(),
                        withdraw.getCreatedAt(), withdraw.getUpdatedAt(), wallet.getId(),
                        withdraw.getReason(), withdraw.getProofImageUrl())));
    }

}
