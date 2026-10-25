package com.pet_project.watchstore.service;

import com.pet_project.watchstore.dto.CreateOrderRequest;
import com.pet_project.watchstore.dto.OrderResponse;
import com.pet_project.watchstore.dto.PageResponse;
import com.pet_project.watchstore.entity.Order;
import com.pet_project.watchstore.entity.OrderItem;
import com.pet_project.watchstore.entity.Product;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.OrderItemRepository;
import com.pet_project.watchstore.repository.OrderRepository;
import com.pet_project.watchstore.repository.ProductRepository;
import com.pet_project.watchstore.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse create(Integer userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Set<Integer> seen = new HashSet<>();
        List<CreateOrderRequest.Item> requestedItems = request.items().stream()
                .sorted((a, b) -> a.productId().compareTo(b.productId()))
                .toList();
        List<Product> products = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.Item item : requestedItems) {
            if (!seen.add(item.productId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm bị lặp trong đơn hàng");
            }
            Product product = productRepository.findActiveByIdForUpdate(item.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm không còn bán"));
            if (product.getStock() < item.quantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sản phẩm không đủ tồn kho");
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
            products.add(product);
        }
        if (total.signum() <= 0 || total.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng tiền đơn hàng không hợp lệ");
        }

        Order order = orderRepository.save(Order.create(user, request.receiverName(), request.phone(),
                request.address(), request.note(), total, request.paymentMethod()));
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < requestedItems.size(); i++) {
            CreateOrderRequest.Item item = requestedItems.get(i);
            Product product = products.get(i);
            product.reserveStock(item.quantity());
            orderItems.add(OrderItem.create(order, product, item.quantity()));
        }
        orderItemRepository.saveAll(orderItems);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> findMine(Integer userId, Pageable pageable) {
        return PageResponse.from(orderRepository.findByUser_Id(userId, pageable).map(OrderResponse::from));
    }
}
