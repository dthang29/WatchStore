package com.pet_project.watchstore.service;

import com.pet_project.watchstore.common.constants.PaymentMethod;
import com.pet_project.watchstore.dto.CreateOrderRequest;
import com.pet_project.watchstore.entity.Order;
import com.pet_project.watchstore.entity.Product;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.OrderItemRepository;
import com.pet_project.watchstore.repository.OrderRepository;
import com.pet_project.watchstore.repository.ProductRepository;
import com.pet_project.watchstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Test
    void orderTotalComesFromLockedProductsAndReservesStock() {
        OrderRepository orders = mock(OrderRepository.class);
        OrderItemRepository orderItems = mock(OrderItemRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        UserRepository users = mock(UserRepository.class);
        OrderService service = new OrderService(orders, orderItems, products, users);
        Product product = mock(Product.class);
        when(users.findById(7)).thenReturn(Optional.of(mock(User.class)));
        when(products.findActiveByIdForUpdate(12)).thenReturn(Optional.of(product));
        when(product.getStock()).thenReturn(3);
        when(product.getPrice()).thenReturn(new BigDecimal("125000.00"));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(7, request(List.of(new CreateOrderRequest.Item(12, 2))));

        assertEquals(new BigDecimal("250000.00"), response.totalAmount());
        verify(product).reserveStock(2);
        verify(orderItems).saveAll(any());
    }

    @Test
    void rejectsInsufficientStockBeforeSavingOrder() {
        OrderRepository orders = mock(OrderRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        UserRepository users = mock(UserRepository.class);
        OrderService service = new OrderService(orders, mock(OrderItemRepository.class), products, users);
        Product product = mock(Product.class);
        when(users.findById(7)).thenReturn(Optional.of(mock(User.class)));
        when(products.findActiveByIdForUpdate(12)).thenReturn(Optional.of(product));
        when(product.getStock()).thenReturn(1);

        assertThrows(ResponseStatusException.class,
                () -> service.create(7, request(List.of(new CreateOrderRequest.Item(12, 2)))));
        org.mockito.Mockito.verifyNoInteractions(orders);
    }

    private static CreateOrderRequest request(List<CreateOrderRequest.Item> items) {
        return new CreateOrderRequest("Customer", "0900000000", "123 Street", null,
                PaymentMethod.VNPAY, items);
    }
}
