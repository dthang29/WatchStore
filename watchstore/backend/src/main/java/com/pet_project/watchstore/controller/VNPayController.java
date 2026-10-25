package com.pet_project.watchstore.controller;

import com.pet_project.watchstore.config.AppSecurityProperties;
import com.pet_project.watchstore.security.AuthUser;
import com.pet_project.watchstore.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {

    private final VNPayService vnPayService;
    private final AppSecurityProperties securityProperties;

    public VNPayController(VNPayService vnPayService, AppSecurityProperties securityProperties) {
        this.vnPayService = vnPayService;
        this.securityProperties = securityProperties;
    }

    @PostMapping("/orders/{orderId}/payment-url")
    public PaymentUrlResponse paymentUrl(@PathVariable Integer orderId,
                                         @AuthenticationPrincipal AuthUser me,
                                         HttpServletRequest request) {
        return new PaymentUrlResponse(vnPayService.createPaymentUrl(orderId, me.id(), request.getRemoteAddr()));
    }

    @GetMapping("/ipn")
    public VNPayService.IpnResponse ipn(@RequestParam Map<String, String> params) {
        try {
            return vnPayService.handleIpn(params);
        } catch (RuntimeException ex) {
            return new VNPayService.IpnResponse("99", "System error");
        }
    }

    @GetMapping("/return")
    public ResponseEntity<Void> paymentReturn(@RequestParam Map<String, String> params) {
        String status = vnPayService.returnStatus(params);
        String frontend = securityProperties.frontendUrl().replaceAll("/+$", "");
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(frontend + "/account/orders?payment=" + status).toString())
                .build();
    }

    public record PaymentUrlResponse(String paymentUrl) {
    }
}
