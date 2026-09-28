package com.system.hotel_reservation.controller;

import com.system.hotel_reservation.dto.PaymentRequest;
import com.system.hotel_reservation.entity.Payment;
import com.system.hotel_reservation.payment.PaystackResponse;
import com.system.hotel_reservation.payment.PaystackService;
import com.system.hotel_reservation.service.PaymentService;
import com.system.hotel_reservation.service.StaffPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;
	private final PaystackService paystackService;
	private final StaffPolicyService staffPolicyService;

	public PaymentController(PaymentService paymentService,
			PaystackService paystackService,
			StaffPolicyService staffPolicyService) {
		this.paymentService = paymentService;
		this.paystackService = paystackService;
		this.staffPolicyService = staffPolicyService;
	}

	@PostMapping
	public ResponseEntity<Payment> createPayment(
			@RequestBody PaymentRequest request) {
		staffPolicyService.require("takePayments");
		return ResponseEntity.ok(paymentService.createPayment(request));
	}

	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(@RequestBody String body,
			@org.springframework.web.bind.annotation.RequestHeader(value = "x-paystack-signature", required = false) String signature) {
		paystackService.webhook(body, signature);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<List<Payment>> getAllPayments() {
		return ResponseEntity.ok(paymentService.getAllPayments());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
		return ResponseEntity.of(paymentService.getPaymentById(id));
	}

	@PatchMapping("/{id}/success")
	public ResponseEntity<Payment> markPaymentSuccessful(@PathVariable Long id) {
		return ResponseEntity.ok(paymentService.markPaymentSuccessful(id));
	}

	@PostMapping("/initialize/{bookingId}")
	public ResponseEntity<PaystackResponse> initializePayment(
			@PathVariable Long bookingId) {
		return ResponseEntity.ok(paystackService.initializePayment(bookingId));
	}

	@GetMapping("/verify/{reference}")
	public ResponseEntity<PaystackResponse> verifyPayment(
			@PathVariable String reference) {
		return ResponseEntity.ok(paystackService.verifyPayment(reference));
	}
}
