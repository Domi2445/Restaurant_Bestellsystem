package org.zimtkoriander.bestellsystem.controller;

import Model.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zimtkoriander.bestellsystem.dto.WinOrderOrderResponse;
import org.zimtkoriander.bestellsystem.repository.OrderRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/winorder")
public class WinOrderController {
    private final OrderRepository orderRepository;

    public WinOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<WinOrderOrderResponse> exportOrder(@PathVariable UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> ResponseEntity.ok(toWinOrder(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/orders/import")
    public ResponseEntity<WinOrderOrderResponse> importOrder(@RequestBody WinOrderOrderResponse request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStoreId(UUID.randomUUID());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(Model.OrderStatus.CREATED);
        Order saved = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(toWinOrder(saved));
    }

    private WinOrderOrderResponse toWinOrder(Order order) {
        return new WinOrderOrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}

