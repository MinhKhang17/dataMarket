package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.BankRequest;
import com.example.datasetapi.dto.request.ProcessWithdrawRequest;
import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.WithdrawListResponse;
import com.example.datasetapi.dto.response.WithdrawResponse;
import com.example.datasetapi.enums.Datasets.BuyType;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.BankAccount;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.model.paySystem.Withdraw;
import com.example.datasetapi.model.paySystem.WithdrawOtp;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.WithdrawOtpRepository;
import com.example.datasetapi.repository.WithdrawRepository;
import com.example.datasetapi.service.feature.EmailService;
import com.example.datasetapi.service.feature.ImageService;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
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
    private final WithdrawOtpRepository otpRepository;
    private final EmailService emailService;

    @Override
    public void sendRequestMail() {
        String token = tokenService.resolveToken(request);
        if(token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.UNAUTHORIZED);
        }

        User user = userService.findUserById(userId);
        emailService.sendWithdrawOtp(userId, user.getEmail());
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> withdrawRequest(WithdrawRequest withdrawRequest) {
            String token = tokenService.resolveToken(request);
            if(token == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.INVALID_TOKEN);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED,ErrorCode.UNAUTHORIZED);
            }

            WithdrawOtp otp = otpRepository.findByUserIdOrderByExpireAtDesc(userId)
                    .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST,ErrorCode.WITHDRAW_OTP_NOT_FOUND));

            if(!otp.getOtp().equals(withdrawRequest.getOtp())) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_OTP);
            }
            if (Instant.now().isAfter(otp.getExpireAt())) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.OTP_EXPIRED);
            }

            otpRepository.delete(otp);

            Wallet wallet = walletService.findWalletByUserId(userId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

            if (!wallet.getUser().getId().equals(userId)) {
                throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
            }
            Long amount = withdrawRequest.getAmount();
            if (amount == null || amount <= 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_WITHDRAW_AMOUNT);
            }
            if (wallet.getBalance() < amount) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_FUNDS);
            }

            BankAccount bankAccount = null;
            if(withdrawRequest.getBankAccountId() != null) {
                bankAccount = bankAccountService.getBankAccount(withdrawRequest.getBankAccountId());
                if(bankAccount == null) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.BANK_ACCOUNT_NOT_FOUND);
                }
                if (!bankAccount.getUser().getId().equals(userId))
                    throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_OWNER_BANK_ACCOUNT);
            }

            paymentService.updateWallet(TransferType.WITHDRAW_HOLD, amount, userId, BuyType.WITHDRAW);


            Withdraw withdraw = new Withdraw();
            withdraw.setUser(wallet.getUser());
            withdraw.setWallet(wallet);
            withdraw.setAmount(withdrawRequest.getAmount());
            withdraw.setBankAccount(bankAccount);
            withdraw.setStatus(Withdraw.Status.PENDING);
            withdrawRepository.saveAndFlush(withdraw);

            return ResponseEntity.ok(new ApiResponse(true, "Success",
                    new WithdrawResponse(
                            withdraw.getId(),
                            withdraw.getStatus().name(),
                            withdraw.getAmount(),
                            withdraw.getCreatedAt(),
                            withdraw.getUpdatedAt(), wallet.getId(),
                            null,
                            null,
                            withdraw.getBankAccount().getBankName() ,
                            withdraw.getBankAccount().getAccountNumber(),
                            wallet.getBalance(),
                            wallet.getHoldBalance())));
    }



    @Override
    @Transactional
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
        paymentService.updateWallet(TransferType.WITHDRAW_APPROVE, withdraw.getAmount(), withdraw.getUser().getId(), BuyType.OTHER);
        withdrawRepository.saveAndFlush(withdraw);

        Wallet wallet = walletService.findWalletByUserId(withdraw.getUser().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

        return ResponseEntity.ok(new ApiResponse(true, "Withdraw approved successfully", new WithdrawResponse(
                withdraw.getId(),
                withdraw.getStatus().name(),
                withdraw.getAmount(),
                withdraw.getCreatedAt(),
                withdraw.getUpdatedAt(),
                withdraw.getWallet().getId(),
                withdraw.getReason(),
                withdraw.getProofImageUrl(),
                withdraw.getBankAccount().getBankName(),
                withdraw.getBankAccount().getAccountNumber(),
                wallet.getBalance(),
                wallet.getHoldBalance()
        )));

    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> processWithdrawReject(ProcessWithdrawRequest withdrawRequest) {
        User admin = validateAdmin();
        Withdraw withdraw = getPendingWithdraw(withdrawRequest.getId());
        withdraw.setStatus(Withdraw.Status.REJECT);
        if(withdrawRequest.getReason() == null || withdrawRequest.getReason().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REASON);
        }
        withdraw.setReason(withdrawRequest.getReason());
        withdrawRepository.saveAndFlush(withdraw);

        paymentService.updateWallet(TransferType.WITHDRAW_REJECT, withdraw.getAmount(), withdraw.getUser().getId(), BuyType.WITHDRAW);

        Wallet wallet = walletService.findWalletByUserId(withdraw.getUser().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,ErrorCode.WALLET_NOT_FOUND));

        return ResponseEntity.ok(new ApiResponse(true, "Withdraw rejected successfully", new WithdrawResponse(
                withdraw.getId(),
                withdraw.getStatus().name(),
                withdraw.getAmount(),
                withdraw.getCreatedAt(),
                withdraw.getUpdatedAt(),
                withdraw.getWallet().getId(),
                withdraw.getReason(),
                withdraw.getProofImageUrl(),
                withdraw.getBankAccount().getBankName(),
                withdraw.getBankAccount().getAccountNumber(),
                wallet.getBalance(),
                wallet.getHoldBalance()
        )));
    }

    @Override
    public ResponseEntity<ApiResponse> listWithdraws(String status) {
        String token = tokenService.resolveToken(request);
        if (token == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);

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
        if(withdraws.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse(true, "There are not any request", null));
        }

        List<WithdrawListResponse> responses = withdraws.stream()
                .map(w -> new WithdrawListResponse(
                        w.getId(),
                        w.getStatus().name(),
                        w.getAmount(),
                        w.getCreatedAt(),
                        w.getUpdatedAt()
                )).toList();

        return ResponseEntity.ok(new ApiResponse(true, "Success", responses));
    }

    @Override
    public ResponseEntity<ApiResponse> getWithdrawById(Long id) {
        String token = tokenService.resolveToken(request);
        if(token == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);

        Long userId = jwtUtil.getUserIdFromToken(token);
        if(userId == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);


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
                        withdraw.getBankAccount().getBankName(),
                        withdraw.getBankAccount().getAccountNumber(),
                        withdraw.getWallet().getBalance(),
                        withdraw.getWallet().getHoldBalance()
                )
        ));
    }


    private User validateAdmin() {
        String token = tokenService.resolveToken(request);
        if (token == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);

        Long adminId = jwtUtil.getUserIdFromToken(token);
        if (adminId == null) throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);

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
