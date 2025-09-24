package com.example.datasetapi.service.payment;

import com.example.datasetapi.config.VnpayProperties;
import com.example.datasetapi.dto.request.CreateVnpayPaymentRequest;
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

    public VnPayService(VnpayProperties props) {
        this.props = props;
    }

    public String createPaymentUrl(HttpServletRequest servletRequest, CreateVnpayPaymentRequest reqBody) {
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
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", "VND");

        if (!bankCode.isEmpty()) {
            params.put("vnp_BankCode", bankCode);
        }
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
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
}
