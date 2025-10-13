package com.example.datasetapi.service.payment;

import com.example.datasetapi.config.VnpayProperties;
import com.example.datasetapi.dto.request.CreateVnpayPaymentRequest;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.paySystem.Transaction;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.TransactionRepository;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.TokenServiceImpl;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnPayService {

    private final VnpayProperties props;
    private final PaymentService paymentService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final HttpServletRequest request;
    private final TokenServiceImpl tokenServiceImpl;

    public VnPayService(VnpayProperties props,
                        PaymentService paymentService,
                        WalletRepository walletRepository, TransactionRepository transactionRepository,
                        UserService userService,
                        JwtUtil jwtUtil,
                        TokenServiceImpl tokenServiceImpl, TokenService tokenService, HttpServletRequest request) {
        this.props = props;
        this.paymentService = paymentService;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.request = request;
        this.tokenServiceImpl = tokenServiceImpl;
    }

    // Tạo URL thanh toán
    public String createPaymentUrl(HttpServletRequest servletRequest, CreateVnpayPaymentRequest reqBody) {
        String token = tokenService.resolveToken(request);
        if(token == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        BigDecimal inVnd = new BigDecimal(safe(reqBody.getAmount()));
        if (inVnd.scale() > 0 || inVnd.signum() <= 0) {
            throw new IllegalArgumentException("amount invalid");
        }
        long vnpAmount = inVnd.multiply(BigDecimal.valueOf(100)).longValueExact();

        ZoneId tz = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnpCreate = ZonedDateTime.now(tz).format(fmt);
        String vnpExpire = ZonedDateTime.now(tz).plusMinutes(15).format(fmt);

        String txnRef = String.format("%08d", new Random().nextInt(100_000_000));

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_OrderInfo", "UID:" + userId + "|" + safe(reqBody.getOrderInfo()));
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", "VND");
        if (!safe(reqBody.getBankCode()).isEmpty()) {
            params.put("vnp_BankCode", reqBody.getBankCode());
        }
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderType", safe(reqBody.getOrderType()));
        params.put("vnp_Locale", safe(reqBody.getLanguage().isEmpty() ? "vn" : reqBody.getLanguage()));
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", getClientIp(servletRequest));
        params.put("vnp_CreateDate", vnpCreate);
        params.put("vnp_ExpireDate", vnpExpire);

        String hashData = buildDataToHash(params);
        String secureHash = hmacSHA512(props.getHashSecret(), hashData);
        String query = buildQuery(params) + "&vnp_SecureHash=" + urlEncode(secureHash);

        return props.getPayUrl() + "?" + query;
    }

    // Xử lý IPN/Return
    public Map<String, String> handleIpn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();

        // 1. Verify chữ ký
        if (!verifySignature(params)) {
            result.put("RspCode", "97");
            result.put("Message", "Invalid signature");
            return result;
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");

        if ("00".equals(responseCode)) {
            // 2. Check transaction đã xử lý chưa
            if (transactionRepository.existsByTxnRef(txnRef)) {
                result.put("RspCode", "00");
                result.put("Message", "Transaction already confirmed");
                return result;
            }

            // 3. Quy đổi point
            long amountVnd = Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100;
            long points = amountVnd / 1000;

            Long uid = extractUserId(params.get("vnp_OrderInfo"));
            if (uid != null) {
                Wallet wallet = walletRepository.findByUserId(uid).orElseGet(() -> {
                    Wallet w = new Wallet();
                    User u = userService.findUserById(uid)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    w.setUser(u);
                    w.setAmount(0L);
                    return walletRepository.save(w);
                });

                // 4. Update wallet
                wallet.setAmount(wallet.getAmount() + points);
                walletRepository.save(wallet);

                // 5. Save transaction kèm createdAt
                Transaction t = new Transaction();
                t.setAmount(points);
                t.setType(TransferType.TOUP);
                t.setWallet(wallet);
                t.setTxnRef(txnRef);
                // createdAt sẽ tự set nhờ @PrePersist
                transactionRepository.save(t);

                System.out.println("VNPay IPN OK: uid=" + uid + " points=" + points + " txnRef=" + txnRef);
            }
        }

        result.put("RspCode", "00");
        result.put("Message", "Confirm Success");
        return result;
    }


    // Helpers
    private static String safe(String s) { return (s == null) ? "" : s.trim(); }
    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) return ip.split(",")[0].trim();
        return request.getRemoteAddr();
    }
    private static String urlEncode(String s) {
        try { return URLEncoder.encode(s, StandardCharsets.UTF_8.toString()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    private static String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder(); boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append('&');
            sb.append(urlEncode(e.getKey())).append('=').append(urlEncode(e.getValue()));
            first = false;
        } return sb.toString();
    }
    private static String buildDataToHash(Map<String, String> params) { return buildQuery(params); }
    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) { sb.append(String.format("%02x", b & 0xff)); }
            return sb.toString();
        } catch (Exception ex) { throw new RuntimeException("Error while calculating HMAC SHA512", ex); }
    }
    public boolean verifySignature(Map<String, String> allParams) {
        Map<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : allParams.entrySet()) {
            String key = e.getKey();
            if ("vnp_SecureHash".equalsIgnoreCase(key) || "vnp_SecureHashType".equalsIgnoreCase(key)) continue;
            if (e.getValue() != null && !e.getValue().isEmpty()) sorted.put(key, e.getValue());
        }
        String data = buildDataToHash(sorted);
        String expectedHash = hmacSHA512(props.getHashSecret(), data);
        String actualHash = allParams.get("vnp_SecureHash");
        return expectedHash.equalsIgnoreCase(actualHash);
    }
    private Long extractUserId(String orderInfo) {
        if (orderInfo != null && orderInfo.startsWith("UID:")) {
            try { return Long.parseLong(orderInfo.split("\\|")[0].split(":")[1]); }
            catch (Exception ignored) {}
        }
        return null;
    }
}
