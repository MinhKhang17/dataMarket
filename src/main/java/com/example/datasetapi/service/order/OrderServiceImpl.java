package com.example.datasetapi.service.order;

import com.example.datasetapi.dto.request.OrderRequest;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.enums.Datasets.PricingMethod;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.order.Order;
import com.example.datasetapi.model.order.OrderItem;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.OrderRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final TokenService tokenService;

    @Override
    public ResponseEntity<ApiResponse> getAllOrders() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long adminId = jwtUtil.getUserIdFromToken(token);
        User admin = userService.findUserById(adminId);
        if (!admin.getRole().getName().equalsIgnoreCase("ADMIN")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        List<Order> orders = orderRepository.findAll();
        List<OrderSummaryResponse> response = orders.stream()
                .map(o ->
                     new OrderSummaryResponse(
                            o.getId(),
                            o.getUser().getUsername(),
                            o.getCreatedAt(),
                            o.getTotalAmount(),
                            o.getPurchaseMethod(),
                            o.getItems().size()
                    )
                ).toList();

        return ResponseEntity.ok().body(new ApiResponse(true, "Orders retrieved successfully", response));
    }

    @Override
    public ResponseEntity<ApiResponse> getOrdersForConsumer() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        User consumer = userService.findUserById(userId);
        if (!consumer.getRole().getName().equalsIgnoreCase("CONSUMER")) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        List<Order> orders = orderRepository.findByUserId(userId);
        if(orders.isEmpty()) {
            return ResponseEntity.ok().body(new ApiResponse(true, "No orders available", List.of()));
        }

        List<OrderSummaryResponse> response = orders.stream()
                .map(o ->
                     new OrderSummaryResponse(
                             o.getId(),
                             o.getUser().getUsername(),
                             o.getCreatedAt(),
                             o.getTotalAmount(),
                             o.getPurchaseMethod(),
                             o.getItems().size()
                     )).toList();

        return ResponseEntity.ok().body(new ApiResponse(true, "Orders retrieved successfully", response));
    }

    @Override
    @Transactional
    public ConsumerOrderResponse createOrder(Long userId, List<OrderRequest> orderRequest) {

        User user = userService.findUserById(userId);

        if (orderRequest == null || orderRequest.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD);
        }

        Order datasetOrder = new Order();
        datasetOrder.setUser(user);

        List<OrderItem> items = orderRequest.stream()
                .map(req -> {
                    OrderItem item = new OrderItem();
                    item.setDatasetId(req.getDatasetId());
                    item.setDatasetName(req.getDatasetName());
                    item.setPriceAtPurchase(req.getPrice());
                    item.setOrder(datasetOrder);
                    return item;
                }).toList();

        datasetOrder.setItems(items);

        double total = items.stream()
                .mapToDouble(OrderItem::getPriceAtPurchase)
                .sum();
        datasetOrder.setTotalAmount(total);

        PricingMethod method = orderRequest.get(0).getPricingMethod();
        boolean allSame = orderRequest.stream()
                .allMatch(o -> o.getPricingMethod().equals(method));

        if (!allSame) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PRICING_METHOD);
        }
        datasetOrder.setPurchaseMethod(method);
        Order saved = orderRepository.save(datasetOrder);

        List<OrderItemDetailResponse> itemsDetail = items.stream()
                .map(i -> {
                    return new OrderItemDetailResponse(
                            i.getDatasetId(),
                            i.getDatasetName(),
                            i.getPriceAtPurchase()
                    );
                }).toList();

        return new ConsumerOrderResponse(
                saved.getId(),
                saved.getTotalAmount(),
                itemsDetail,
                saved.getPurchaseMethod(),
                saved.getCreatedAt()
        );
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
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));

            User requester = order.getUser();

            if(!user.getRole().getName().equalsIgnoreCase("ADMIN")
                    && !requester.getId().equals(user.getId())) {
                throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
            }

            List<OrderItemDetailResponse> itemsDetail = order.getItems().stream()
                    .map(i ->
                         new OrderItemDetailResponse(
                                i.getDatasetId(),
                                i.getDatasetName(),
                                i.getPriceAtPurchase()
                        )).toList();

            if(user.getRole().getName().equalsIgnoreCase("CONSUMER")) {
                ConsumerOrderResponse consumerResponse = new ConsumerOrderResponse(
                        orderId,
                        order.getTotalAmount(),
                        itemsDetail,
                        order.getPurchaseMethod(),
                        order.getCreatedAt());
                return ResponseEntity.ok().body(new ApiResponse(true, "Order retrieved successfully", consumerResponse));
            }
            AdminOrderResponse adminResponse = new AdminOrderResponse(
                    order.getId(),
                    order.getUser().getId(),
                    order.getUser().getUsername(),
                    order.getUser().getEmail(),
                    itemsDetail,
                    order.getTotalAmount(),
                    order.getPurchaseMethod(),
                    order.getCreatedAt()
            );
            return ResponseEntity.ok().body(new ApiResponse(true, "Order retrieved successfully", adminResponse));
    }

    @Override
    public List<Order> findByPricingMethod(String pricingMethod) {
        return orderRepository.findByPurchaseMethod(PricingMethod.valueOf(pricingMethod));
    }
}
