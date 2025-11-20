package com.example.datasetapi.controller.adminController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.VerificationStatus.RegistrationStatus;
import com.example.datasetapi.mapper.UserMapper;
import com.example.datasetapi.service.order.OrderService;
import com.example.datasetapi.service.user.AdminService;
import com.example.datasetapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("providerRegis/getPending")
    public ResponseEntity<?> getProviderRegisPending() {
        return ResponseEntity.ok().body(new ApiResponse(true, "Provider Pending status load success", adminService.getProviderRegisPending(RegistrationStatus.PENDING)));
    }

    @PostMapping("providerRegis/accept")
    public ResponseEntity<?> acceptProviderRegis(@RequestParam("providerRegistrationId") long providerRegistrationId, HttpServletRequest request) {
        return adminService.acceptProviderRegis(providerRegistrationId, request);
    }

    @PostMapping("providerRegis/reject")
    public ResponseEntity<?> rejectProviderRegis(@RequestParam("providerRegistrationId") long providerRegistrationId, @RequestParam("reason") String reason, HttpServletRequest request) {
    return adminService.rejectProviderRegis(providerRegistrationId,reason,request);
    }

    @GetMapping("providerRegis/getReview")
    public ResponseEntity<?> getProviderReview() {
        return adminService.getReviewProviderHistory();
    }

    @GetMapping("providerRegis/get")
    public ResponseEntity<?> getProviderRegisAcc() {
        return ResponseEntity.ok().body(new ApiResponse(true, "Provider Pending status load success", adminService.getProviderRegisPending(RegistrationStatus.APPROVED)));
    }

    @GetMapping("orders/get")
    public ResponseEntity<ApiResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("orders/get/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }
    @GetMapping("users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok().body(userService.findAllUser());
    }
    @GetMapping("user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userMapper.toUserDto(userService.findUserById(id)));
    }
    @PostMapping("user/banning/{id}")
    public ResponseEntity<?> banning(@PathVariable Long id) {
        return ResponseEntity.ok().body(userMapper.toUserDto(userService.banUser(id)));
    }
}
