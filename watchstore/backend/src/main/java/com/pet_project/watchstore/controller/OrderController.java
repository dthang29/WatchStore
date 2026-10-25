package com.pet_project.watchstore.controller;

import com.pet_project.watchstore.dto.CreateOrderRequest;
import com.pet_project.watchstore.dto.OrderResponse;
import com.pet_project.watchstore.dto.PageResponse;
import com.pet_project.watchstore.security.AuthUser;
import com.pet_project.watchstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal AuthUser me,
                                @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(me.id(), request);
    }

    @GetMapping("/me")
    public PageResponse<OrderResponse> findMine(@AuthenticationPrincipal AuthUser me,
                                                @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                                Pageable pageable) {
        return orderService.findMine(me.id(), pageable);
    }
}
