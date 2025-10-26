package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.ProcessWithdrawRequest;
import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.WithdrawResponse;
import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.BankAccount;
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
import java.util.List;

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
    private final BankAccountService bankAccountService;

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
            if(withdrawRequest.getBank() == null || withdrawRequest.getBank().isBlank()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
            }
            if(withdrawRequest.getAccountNumber() == null || withdrawRequest.getAccountNumber().isBlank()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
            }

            Withdraw withdraw = new Withdraw();
            withdraw.setUser(wallet.getUser());
            withdraw.setWallet(wallet);
            withdraw.setAmount(withdrawRequest.getAmount());
            withdraw.setBank(withdrawRequest.getBank());
            withdraw.setAccountNumber(withdrawRequest.getAccountNumber());
            withdraw.setStatus(Withdraw.Status.PENDING);
            withdrawRepository.saveAndFlush(withdraw);

            return ResponseEntity.ok(new ApiResponse(true, "Success", new WithdrawResponse(withdraw.getId(), withdraw.getStatus().name(), withdraw.getAmount(), withdraw.getCreatedAt(), withdraw.getUpdatedAt(), wallet.getId(), null, null, withdraw.getBank(),
                    withdraw.getAccountNumber())));
    }

    @Override
    public ResponseEntity<ApiResponse> processWithdrawApprove(ProcessWithdrawRequest withdrawRequest, MultipartFile file) throws IOException {
        User admin = validateAdmin();
        Withdraw withdraw = getPendingWithdraw(withdrawRequest.getId());
        if(file == null || file.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_FILE);
        }
        String fileUrl = imageService.uploadImage(file);
        withdraw.setStatus(Withdraw.Status.APPROVE);
        withdraw.setProofImageUrl(fileUrl);
        if (withdrawRequest.getReason() != null && !withdrawRequest.getReason().isBlank()) {
            withdraw.setReason(withdrawRequest.getReason());
        }
        paymentService.updateWallet(TransferType.WITHDRAW, withdraw.getAmount(), withdraw.getUser().getId(), BuyType.OTHER);
        withdrawRepository.saveAndFlush(withdraw);
        return ResponseEntity.ok(new ApiResponse(true, "Withdraw approved successfully", new WithdrawResponse(
                withdraw.getId(),
                withdraw.getStatus().name(),
                withdraw.getAmount(),
                withdraw.getCreatedAt(),
                withdraw.getUpdatedAt(),
                withdraw.getWallet().getId(),
                withdraw.getReason(),
                withdraw.getProofImageUrl(),
                withdraw.getBank(),
                withdraw.getAccountNumber()
        )));

    }

    @Override
    public ResponseEntity<ApiResponse> processWithdrawReject(ProcessWithdrawRequest withdrawRequest) {
        User admin = validateAdmin();
        Withdraw withdraw = getPendingWithdraw(withdrawRequest.getId());
        withdraw.setStatus(Withdraw.Status.REJECT);
        if(withdrawRequest.getReason() == null || withdrawRequest.getReason().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REASON);
        }
        withdraw.setReason(withdrawRequest.getReason());
        withdrawRepository.saveAndFlush(withdraw);
        return ResponseEntity.ok(new ApiResponse(true, "Withdraw rejected successfully", new WithdrawResponse(
                withdraw.getId(),
                withdraw.getStatus().name(),
                withdraw.getAmount(),
                withdraw.getCreatedAt(),
                withdraw.getUpdatedAt(),
                withdraw.getWallet().getId(),
                withdraw.getReason(),
                withdraw.getProofImageUrl(),
                withdraw.getBank(),
                withdraw.getAccountNumber()
        )));

    }


    @Override
    public ResponseEntity<ApiResponse> listWithdraws(String status) {
        String token = tokenService.resolveToken(request);
        if (token == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);

        User currentUser = userService.findUserById(userId);

        Withdraw.Status enumStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                enumStatus = Withdraw.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_INPUT);
            }
        }

        List<Withdraw> withdraws;
        if (currentUser.getRole().getName().contains("ADMIN")) {
            if(enumStatus == null) {
                withdraws = withdrawRepository.findAll();
            } else {
                withdraws = withdrawRepository.findWithdrawByStatus(enumStatus);
            }
        } else {
            if(enumStatus == null) {
                withdraws = withdrawRepository.findWithdrawByUserId(userId);
            } else  {
                withdraws = withdrawRepository.findWithdrawByUserIdAndStatus(userId, enumStatus);
            }
        }
        if(withdraws == null) {
            return ResponseEntity.ok(new ApiResponse(true, "There are not any request", null));
        }

        List<WithdrawResponse> responses = withdraws.stream()
                .map(w -> new WithdrawResponse(
                        w.getId(),
                        w.getStatus().name(),
                        w.getAmount(),
                        w.getCreatedAt(),
                        w.getUpdatedAt(),
                        w.getWallet().getId(),
                        w.getReason(),
                        w.getProofImageUrl(),
                        w.getBank(),
                        w.getAccountNumber()
                )).toList();

        return ResponseEntity.ok(new ApiResponse(true, "Success", responses));
    }

    @Override
    public ResponseEntity<ApiResponse> getWithdrawById(Long id) {
        String token = tokenService.resolveToken(request);
        if(token == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);

        Long userId = jwtUtil.getUserIdFromToken(token);
        if(userId == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);


        Withdraw withdraw = withdrawRepository.findById(id).orElseThrow(()
                ->new CustomException(HttpStatus.NOT_FOUND, ErrorCode.WITHDRAW_NOT_FOUND));

        User requester = withdraw.getUser();
        User currentUser = userService.findUserById(userId);

        if (!currentUser.getRole().getName().contains("ADMIN")
                && !requester.getId().equals(currentUser.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }

        return ResponseEntity.ok(new ApiResponse(true, "Withdraw detail retrieved successfully",
                new WithdrawResponse(withdraw.getId(),
                        withdraw.getStatus().name(),
                        withdraw.getAmount(),
                        withdraw.getCreatedAt(),
                        withdraw.getUpdatedAt(),
                        withdraw.getWallet().getId(),
                        withdraw.getReason(),
                        withdraw.getProofImageUrl(),
                        withdraw.getBank(),
                        withdraw.getAccountNumber()
                )
        ));
    }


    private User validateAdmin() {
        String token = tokenService.resolveToken(request);
        if (token == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);

        Long adminId = jwtUtil.getUserIdFromToken(token);
        if (adminId == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);

        User admin = userService.findUserById(adminId);
        if (!admin.getRole().getName().contains("ADMIN")) {
            throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }
        return admin;
    }

    private Withdraw getPendingWithdraw(Long id) {
        Withdraw withdraw = withdrawRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.WITHDRAW_NOT_FOUND));

        if(withdraw.getStatus() != Withdraw.Status.PENDING) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.ALREADY_PROCESSED);
        }
        return withdraw;
    }

}
