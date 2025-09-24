package com.example.datasetapi.controller.PaymentController;

import com.example.datasetapi.dto.request.CreateVnpayPaymentRequest;
import com.example.datasetapi.service.payment.VnPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", "00");
        resp.put("message", "success");
        resp.put("data", paymentUrl);

        return ResponseEntity.ok(resp);
    }

    // TODO: thêm /return và /ipn callback handler nếu cần
}