package com.example.datasetapi.controller.payment;

import com.example.datasetapi.dto.request.CreateVnpayPaymentRequest;
import com.example.datasetapi.service.payment.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class VnPayController {

    private final VnPayService vnPayService;

    public VnPayController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(HttpServletRequest req,
                                           @RequestBody CreateVnpayPaymentRequest body) {
        String paymentUrl = vnPayService.createPaymentUrl(req, body);
        return ResponseEntity.ok(Map.of("code", "00", "message", "success", "data", paymentUrl));
    }

    // FE gọi endpoint này khi redirect về
    @GetMapping("/confirm")
    public ResponseEntity<?> confirmPayment(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> { if (v != null && v.length > 0) params.put(k, v[0]); });

        Map<String, String> result = vnPayService.handleIpn(params);

        if ("00".equals(result.get("RspCode"))) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment completed successfully"));
        }
        return ResponseEntity.ok(Map.of("status", "failed", "message", "Payment failed"));
    }

    // Optional: IPN riêng (nếu VNPAY server gọi về)
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> { if (v != null && v.length > 0) params.put(k, v[0]); });
        Map<String, String> result = vnPayService.handleIpn(params);
        return ResponseEntity.ok(result);
    }
}
