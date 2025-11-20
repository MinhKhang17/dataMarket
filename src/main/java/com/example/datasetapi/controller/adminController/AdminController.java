package com.example.datasetapi.controller.adminController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.mapper.UserMapper;
import com.example.datasetapi.model.dataset.PricingRule;
import com.example.datasetapi.service.analytics.AnalyticsService;
import com.example.datasetapi.service.order.OrderService;
import com.example.datasetapi.service.user.AdminService;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired private AnalyticsService analyticsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("providerRegis/getPending")
    public ResponseEntity<?> getProviderRegisPending() {
        return ResponseEntity.ok().body(new ApiResponse(true, "Provider Pending status load success", adminService.getProviderRegisPending(RegistrationStatus.PENDING)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("providerRegis/accept")
    public ResponseEntity<?> acceptProviderRegis(@RequestParam("providerRegistrationId") long providerRegistrationId, HttpServletRequest request) {
        return adminService.acceptProviderRegis(providerRegistrationId, request);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("providerRegis/reject")
    public ResponseEntity<?> rejectProviderRegis(@RequestParam("providerRegistrationId") long providerRegistrationId, @RequestParam("reason") String reason, HttpServletRequest request) {
    return adminService.rejectProviderRegis(providerRegistrationId,reason,request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("providerRegis/getReview")
    public ResponseEntity<?> getProviderReview() {
        return adminService.getReviewProviderHistory();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("providerRegis/get")
    public ResponseEntity<?> getProviderRegisAcc() {
        return ResponseEntity.ok().body(new ApiResponse(true, "Provider Pending status load success", adminService.getProviderRegisPending(RegistrationStatus.APPROVED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("orders/get")
    public ResponseEntity<ApiResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("orders/get/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok().body(userService.findAllUser());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userMapper.toUserDto(userService.findUserById(id)));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("user/banning/{id}")
    public ResponseEntity<?> banning(@PathVariable Long id) {
        return ResponseEntity.ok().body(userMapper.toUserDto(userService.banUser(id)));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("user/unban/{id}")
    public ResponseEntity<?> unban(@PathVariable Long id) {
        return ResponseEntity.ok().body(userMapper.toUserDto(userService.unBanUser(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("pricing-rule")
    public ResponseEntity<?> getPricingRule() {
        return ResponseEntity.ok().body(adminService.getAllPricingRuleForAdmin());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("pricing-rule/{id}")
    public ResponseEntity<?> getDetailPricingRule(@RequestParam long id) {
        return ResponseEntity.ok().body(adminService.getDetailPricingRule(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("pricing-rule/{id}")
    public ResponseEntity<?> updatePricingRule(@RequestBody PricingRule pricingRule) {
        return ResponseEntity.ok().body(adminService.updatePricingRule(pricingRule));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("analytic/revenue")
    public ResponseEntity<?> getRevenue(){
        return ResponseEntity.ok().body(new ApiResponse(true, "Revenue", analyticsService.getRevenue()));
    }
}
