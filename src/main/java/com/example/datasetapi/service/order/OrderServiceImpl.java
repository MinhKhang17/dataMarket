package com.example.datasetapi.service.order;

import com.example.datasetapi.dto.response.AdminOrderResponse;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.ConsumerDatasetOrderResponse;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetOrder;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.DatasetRepository;
import com.example.datasetapi.repository.OrderRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final TokenService tokenService;
    private final DatasetRepository datasetRepository;

    @Override
    public ResponseEntity<ApiResponse> getAllOrders() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long adminId = jwtUtil.getUserIdFromToken(token);
        User admin = userService.findUserById(adminId);
        if (!admin.getRole().getName().equals("ADMIN")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        List<DatasetOrder> orders = orderRepository.findAll();
        List<AdminOrderResponse> response = orders.stream()
                .map(o -> {
                    List<String> datasetNames = o.getDatasets().stream()
                            .map(Dataset::getName)
                            .toList();
                    User user = o.getUser();
                    return new AdminOrderResponse(
                            o.getId(),
                            user.getId(),
                            o.getTotalAmount(),
                            o.getPurchaseMethod(),
                            o.getCreatedAt(),
                            datasetNames,
                            user.getUsername(),
                            user.getEmail());
                }).toList();

        return ResponseEntity.ok().body(new ApiResponse(true, "Orders retrieved successfully", response));
    }

    @Override
    public ResponseEntity<ApiResponse> getOrdersForConsumer() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if(!userService.findUserById(userId).getRole().equals("CONSUMER")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        List<DatasetOrder> orders = orderRepository.findByUserId(userId);
        if(orders.isEmpty()) {
            return ResponseEntity.ok().body(new ApiResponse(false, "Orders not found", List.of()));
        }

        List<ConsumerDatasetOrderResponse> responses = orders.stream()
                .map(o -> {
                    List<String> datasetNames = o.getDatasets().stream()
                            .map(Dataset::getName)
                            .toList();
                    return new ConsumerDatasetOrderResponse(
                            o.getId(),
                            o.getTotalAmount(),
                            datasetNames,
                            o.getPurchaseMethod(),
                            o.getCreatedAt());
                }).toList();
        return ResponseEntity.ok().body(new ApiResponse(true, "Orders retrieved successfully", responses));
    }

    @Override
    public ConsumerDatasetOrderResponse createOrder(Long userId, List<Long> datasetId, Long amount, PricingMethod purchaseMethod) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND);
        }

        if (datasetId == null || datasetId.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        List<Dataset> datasets = datasetRepository.findAllByIdIn(datasetId);
        if (datasets.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.DATASET_NOT_FOUND);
        }
        if(amount == null || amount <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_AMOUNT);
        }
        if(purchaseMethod == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }


        DatasetOrder datasetOrder = new DatasetOrder();
        datasetOrder.setDatasets(datasets);
        datasetOrder.setUser(user);
        datasetOrder.setTotalAmount(amount);
        datasetOrder.setPurchaseMethod(purchaseMethod);

        DatasetOrder saved = orderRepository.save(datasetOrder);

        List<String> datasetNames = datasets.stream()
                .map(Dataset::getName)
                .toList();


        return new ConsumerDatasetOrderResponse(saved.getId(), saved.getTotalAmount(), datasetNames,
                saved.getPurchaseMethod(), saved.getCreatedAt());
    }

    @Override
    public ResponseEntity<ApiResponse> getOrderById(Long orderId) {
        String token = tokenService.resolveToken(request);
            if (token == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            if(userId == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
            }

            User user = userService.findUserById(userId);
            DatasetOrder order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));
            User requester = order.getUser();
            if(!user.getRole().getName().equals("ADMIN")
                    && !requester.getId().equals(user.getId())) {
                throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
            }

            if(user.getRole().getName().equals("CONSUMER")) {
                ConsumerDatasetOrderResponse consumerResponse = new ConsumerDatasetOrderResponse(orderId,
                        order.getTotalAmount(),
                        order.getDatasets().stream().map(Dataset::getName).toList(),
                        order.getPurchaseMethod(),
                        order.getCreatedAt());
                return ResponseEntity.ok().body(new ApiResponse(true, "Order retrieved successfully", consumerResponse));
            }
            AdminOrderResponse adminResponse = new AdminOrderResponse(
                    order.getId(),
                    order.getUser().getId(),
                    order.getTotalAmount(),
                    order.getPurchaseMethod(),
                    order.getCreatedAt(),
                    order.getDatasets().stream().map(Dataset::getName).toList(),
                    order.getUser().getUsername(),
                    order.getUser().getEmail()
            );
            return ResponseEntity.ok().body(new ApiResponse(true, "Order retrieved successfully", adminResponse));
    }
}
