package org.zimtkoriander.bestellsystem.controller;

import Model.Invoice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zimtkoriander.bestellsystem.dto.InvoiceResponse;
import org.zimtkoriander.bestellsystem.service.InvoiceService;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID invoiceId) {
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok(toResponse(invoice));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrder(@PathVariable UUID orderId) {
        Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(toResponse(invoice));
    }

    @PostMapping("/{invoiceId}/send")
    public ResponseEntity<InvoiceResponse> sendInvoice(@PathVariable UUID invoiceId) {
        Invoice invoice = invoiceService.sendInvoice(invoiceId);
        return ResponseEntity.ok(toResponse(invoice));
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getOrderId(),
                invoice.getInvoiceNumber(),
                invoice.getAmount(),
                invoice.getEmail(),
                invoice.getStatus(),
                invoice.getCreatedAt(),
                invoice.getSentAt()
        );
    }
}

