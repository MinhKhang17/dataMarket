package com.example.datasetapi.service.payment;

import com.example.datasetapi.dto.request.WithdrawRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.WithdrawResponse;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.model.paySystem.Withdraw;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.repository.WithdrawRequestRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawRequestServiceImpl implements WithdrawRequestService {
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final WalletRepository walletRepository;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;

    @Override
    public ResponseEntity<ApiResponse> withdrawRequest(WithdrawRequest withdrawRequest) {
            String token = tokenService.resolveToken(request);
            if(token == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            if (withdrawRequest.getAmount() == null || withdrawRequest.getAmount() <= 0) {
                throw new CustomException(ErrorCode.INVALID_WITHDRAW_AMOUNT);
            }
            Wallet wallet = walletRepository.findById(withdrawRequest.getWalletId())
                    .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

            if (wallet == null || !wallet.getUser().getId().equals(userId)) {
                throw new CustomException(ErrorCode.WALLET_NOT_FOUND);
            }
            if (wallet.getAmount() < withdrawRequest.getAmount()) {
                throw new CustomException(ErrorCode.INSUFFICIENT_FUNDS);
            }

            Withdraw withdraw = new Withdraw();
            withdraw.setUser(wallet.getUser());
            withdraw.setWallet(wallet);
            withdraw.setAmount(withdrawRequest.getAmount());
            withdraw.setStatus(Withdraw.Status.PENDING);
            withdrawRequestRepository.saveAndFlush(withdraw);

            return ResponseEntity.ok(new ApiResponse(true, "Success", new WithdrawResponse(withdraw.getId(), withdraw.getStatus().name(), withdraw.getAmount(), withdraw.getCreatedAt(), withdraw.getUpdatedAt(), wallet.getId())));
    }
}
