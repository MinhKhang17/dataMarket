package com.example.datasetapi.controller.PaymentController;

import com.example.datasetapi.dto.request.CreateVnpayPaymentRequest;
import com.example.datasetapi.model.User;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.model.paySystem.Wallet;
import com.example.datasetapi.repository.WalletRepository;
import com.example.datasetapi.enums.TransferType;
import com.example.datasetapi.service.payment.VnPayService;
import com.example.datasetapi.service.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class VnPayController {

    private final VnPayService vnPayService;
    private final WalletRepository walletRepository;
    private final UserService userService;

    public VnPayController(VnPayService vnPayService, WalletRepository walletRepository, UserService userService, PaymentService paymentService) {
        this.vnPayService = vnPayService;
        this.walletRepository = walletRepository;
        this.userService = userService;
        this.paymentService = paymentService;
    }
    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(HttpServletRequest req,
                                           @RequestBody CreateVnpayPaymentRequest body) {
        String paymentUrl = vnPayService.createPaymentUrl(req, body);

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", "00");
        resp.put("message", "success");
        resp.put("data", paymentUrl);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/return")
    public ResponseEntity<?> returnUrl(HttpServletRequest request) {
        // Handle return from VNPay (for user redirect)
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) params.put(k, v[0]);
        });
        
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Payment completed successfully"));
        } else {
            return ResponseEntity.ok().body(Map.of("status", "failed", "message", "Payment failed"));
        }
    }

    @RequestMapping(value = "/ipn", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<Map<String, String>> ipn(HttpServletRequest request) {
        // Lấy toàn bộ params
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) params.put(k, v[0]);
        });

        Map<String, String> result = new HashMap<>();

        // 1) Verify chữ ký
        if (!vnPayService.verifySignature(params)) {
            result.put("RspCode", "97");
            result.put("Message", "Invalid signature");
            return ResponseEntity.ok(result);
        }

        // 2) Xử lý giao dịch
        final String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // Parse amount an toàn
            long amountVnd = 0L;
            try {
                amountVnd = Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100; // VNPAY amount *100
            } catch (NumberFormatException ignore) { /* keep 0 */ }

            // Quy đổi tiền -> point (ví dụ 1,000 VND = 1 point)
            long points = amountVnd / 1000;

            // Lấy userId từ vnp_OrderInfo: "UID:123|..."
            final String orderInfo = params.get("vnp_OrderInfo");
            Long parsedUserId = null;
            if (orderInfo != null && orderInfo.startsWith("UID:")) {
                try {
                    String uidPart = orderInfo.split("\\|")[0]; // "UID:123"
                    parsedUserId = Long.parseLong(uidPart.split(":")[1]);
                } catch (Exception ex) {
                    System.err.println("Cannot parse userId from vnp_OrderInfo=" + orderInfo + " err=" + ex.getMessage());
                }
            }

            // Dùng biến final/effectively final trong lambda
            final Long uid = parsedUserId;

            if (uid != null) {
                // Nếu chưa có ví -> auto tạo (tránh lỗi lambda: dùng uid final)
                walletRepository.findByUserId(uid).orElseGet(() -> {
                    Wallet w = new Wallet();
                    User u = userService.findUserById(uid)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    w.setUser(u);
                    w.setAmount(0L);
                    return walletRepository.save(w);
                });

                // Cộng point
                boolean ok = paymentService.updateWallet(TransferType.TOUP, points, uid);
                System.out.println("VNPay IPN OK: userId=" + uid + ", amountVnd=" + amountVnd + ", points=" + points + ", updated=" + ok);
            } else {
                System.err.println("VNPay IPN: Missing UID in vnp_OrderInfo=" + orderInfo);
            }
        }

        // 3) Phản hồi cho VNPay
        result.put("RspCode", "00");
        result.put("Message", "Confirm Success");
        return ResponseEntity.ok(result);
    }



}