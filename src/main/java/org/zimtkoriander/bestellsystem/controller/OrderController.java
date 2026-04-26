package org.zimtkoriander.bestellsystem.controller;

import Model.Customer;
import Model.Order;
import Model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zimtkoriander.bestellsystem.dto.GuestOrderRequest;
import org.zimtkoriander.bestellsystem.dto.OrderResponse;
import org.zimtkoriander.bestellsystem.repository.CustomerRepository;
import org.zimtkoriander.bestellsystem.repository.OrderRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody Order order) {
        Order saved = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @PostMapping("/guest")
    public ResponseEntity<OrderResponse> createGuestOrder(@RequestBody GuestOrderRequest request) {
        if (request.getFirstName() == null || request.getFirstName().isBlank() ||
                request.getLastName() == null || request.getLastName().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPhone() == null || request.getPhone().isBlank() ||
                request.getStoreId() == null || request.getTotalAmount() == null) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Customer customer = customerRepository.findByEmail(normalizedEmail).orElseGet(() -> {
            Customer c = new Customer();
            c.setFirstName(request.getFirstName().trim());
            c.setLastName(request.getLastName().trim());
            c.setEmail(normalizedEmail);
            c.setPhone(request.getPhone().trim());
            c.setActive(true);
            c.setCreatedAt(Instant.now());
            c.setUpdatedAt(Instant.now());
            return customerRepository.save(c);
        });

        // Keep customer contact data fresh for repeated guest checkouts.
        if (!customer.getFirstName().equals(request.getFirstName()) ||
                !customer.getLastName().equals(request.getLastName()) ||
                !customer.getPhone().equals(request.getPhone())) {
            customer.setFirstName(request.getFirstName().trim());
            customer.setLastName(request.getLastName().trim());
            customer.setPhone(request.getPhone().trim());
            customer.setUpdatedAt(Instant.now());
            customerRepository.save(customer);
        }

        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setStoreId(request.getStoreId());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(OrderStatus.CREATED);

        Order saved = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(mapToResponse(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable UUID customerId) {
        List<OrderResponse> orders = orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStoreId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}

