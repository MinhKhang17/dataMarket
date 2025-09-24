package com.example.datasetapi.controller.PaymentController;

import com.example.datasetapi.dto.request.CreateVnpayPaymentRequest;
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

    public VnPayController(VnPayService vnPayService, PaymentService paymentService) {
        this.vnPayService = vnPayService;
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

    @PostMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(HttpServletRequest request) {
        // Lấy toàn bộ query params từ request
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) params.put(k, v[0]);
        });

        Map<String, String> result = new HashMap<>();

        // 1. Xác thực chữ ký
        if (!vnPayService.verifySignature(params)) {
            result.put("RspCode", "97");
            result.put("Message", "Invalid signature");
            return ResponseEntity.ok(result);
        }

        // 2. Kiểm tra trạng thái giao dịch
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // ✅ Giao dịch thành công
            long amountVnd = Long.parseLong(params.get("vnp_Amount")) / 100;

            // Convert sang point, ví dụ 1,000 VND = 1 point
            long points = amountVnd / 1000;

            // Lấy userId từ vnp_OrderInfo (ví dụ "UID:123")
            String orderInfo = params.get("vnp_OrderInfo");
            Long userId = null;
            if (orderInfo != null && orderInfo.startsWith("UID:")) {
                userId = Long.parseLong(orderInfo.split(":")[1]);
            }

            if (userId != null) {
                // Gọi service update wallet
                paymentService.updateWallet(TransferType.WITHDRAW, points, userId);
            }
        }

        // 3. Trả response về VNPAY
        result.put("RspCode", "00");  // báo đã nhận thành công
        result.put("Message", "Confirm Success");
        return ResponseEntity.ok(result);
    }


}