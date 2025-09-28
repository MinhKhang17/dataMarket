package com.example.datasetapi.service.payment;

import com.example.datasetapi.config.VnpayProperties;
import com.example.datasetapi.dto.request.CreateVnpayPaymentRequest;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.WalletRepository;
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
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenServiceImpl  tokenServiceImpl;

    public VnPayService(VnpayProperties props, PaymentService paymentService, WalletRepository walletRepository, UserService userService, JwtUtil jwtUtil, TokenServiceImpl tokenServiceImpl) {
        this.props = props;
        this.paymentService = paymentService;
        this.walletRepository = walletRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenServiceImpl = tokenServiceImpl;
    }

    public String createPaymentUrl(HttpServletRequest servletRequest, CreateVnpayPaymentRequest reqBody) {

        Long userId = jwtUtil.getUserIdFromToken(tokenServiceImpl.resolveToken(servletRequest));

        // 1. Validate input
        String orderInfo = safe(reqBody.getOrderInfo());
        String orderType = safe(reqBody.getOrderType());
        String bankCode  = safe(reqBody.getBankCode());
        String locale    = safe(reqBody.getLanguage());
        if (!"vn".equalsIgnoreCase(locale) && !"en".equalsIgnoreCase(locale)) locale = "vn";

        BigDecimal inVnd = new BigDecimal(safe(reqBody.getAmount()));
        if (inVnd.scale() > 0 || inVnd.signum() <= 0) {
            throw new IllegalArgumentException("amount invalid");
        }
        long vnpAmount = inVnd.multiply(BigDecimal.valueOf(100)).longValueExact();

        // 2. Time
        ZoneId tz = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnpCreate = ZonedDateTime.now(tz).format(fmt);
        String vnpExpire = ZonedDateTime.now(tz).plusMinutes(15).format(fmt);

        // 3. Random TxnRef
        String txnRef = String.format("%08d", new Random().nextInt(100_000_000));

        // 4. Params
        Map<String, String> params = new TreeMap<>();
        // Use orderInfo from request, but append userId for identification
        String finalOrderInfo = orderInfo.isEmpty() ? "Payment for user " + reqBody.getUserId() : orderInfo;
        params.put("vnp_OrderInfo", "UID:" + userId + "|" + finalOrderInfo);
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", "VND");

        if (!bankCode.isEmpty()) {
            params.put("vnp_BankCode", bankCode);
        }
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderType", orderType);
        params.put("vnp_Locale", locale);
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", getClientIp(servletRequest));
        params.put("vnp_CreateDate", vnpCreate);
        params.put("vnp_ExpireDate", vnpExpire);

        // 5. Build query & sign
        String hashData = buildDataToHash(params);
        String secureHash = hmacSHA512(props.getHashSecret(), hashData);
        String query = buildQuery(params) + "&vnp_SecureHash=" + urlEncode(secureHash);

        return props.getPayUrl() + "?" + query;
    }

    // ===== Helpers =====
    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) return ip.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private static String urlEncode(String s) {
        if (s == null) return "";
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append('&');
            sb.append(urlEncode(e.getKey())).append('=').append(urlEncode(e.getValue()));
            first = false;
        }
        return sb.toString();
    }

    private static String buildDataToHash(Map<String, String> params) {
        return buildQuery(params);
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error while calculating HMAC SHA512", ex);
        }
    }

    public boolean verifySignature(Map<String, String> allParams) {
        // Lấy tất cả params trừ vnp_SecureHash và vnp_SecureHashType
        Map<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : allParams.entrySet()) {
            String key = e.getKey();
            if ("vnp_SecureHash".equalsIgnoreCase(key) || "vnp_SecureHashType".equalsIgnoreCase(key)) {
                continue;
            }
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                sorted.put(key, e.getValue());
            }
        }

        // Build lại query string để hash
        String data = buildDataToHash(sorted);

        // Tính HMAC SHA512 từ data
        String expectedHash = hmacSHA512(props.getHashSecret(), data);

        // Lấy chữ ký do VNPAY gửi
        String actualHash = allParams.get("vnp_SecureHash");

        // So sánh, nếu giống nhau thì hợp lệ
        return expectedHash.equalsIgnoreCase(actualHash);
    }

    private Long extractUserId(String orderInfo) {
        if (orderInfo != null && orderInfo.startsWith("UID:")) {
            try {
                String uidPart = orderInfo.split("\\|")[0];
                return Long.parseLong(uidPart.split(":")[1]);
            } catch (Exception ex) {
                System.err.println("Cannot parse userId from vnp_OrderInfo=" + orderInfo + " err=" + ex.getMessage());
            }
        }
        return null;
    }


    public Map<String, String> handleIpn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();

        // Verify chữ ký
        if (!verifySignature(params)) {
            result.put("RspCode", "97");
            result.put("Message", "Invalid signature");
            return result;
        }

        if ("00".equals(params.get("vnp_ResponseCode"))) {
            long amountVnd = 0L;
            try {
                amountVnd = Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100;
            } catch (NumberFormatException ignore) {}
            long points = amountVnd / 1000;

            Long uid = extractUserId(params.get("vnp_OrderInfo"));
            if (uid != null) {
                walletRepository.findByUserId(uid).orElseGet(() -> {
                    Wallet w = new Wallet();
                    User u = userService.findUserById(uid)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    w.setUser(u);
                    w.setAmount(0L);
                    return walletRepository.save(w);
                });

                paymentService.updateWallet(TransferType.TOUP, points, uid);
            }
        }

        result.put("RspCode", "00");
        result.put("Message", "Confirm Success");
        return result;
    }
}
