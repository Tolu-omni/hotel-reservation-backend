package com.system.hotel_reservation.service;

import com.system.hotel_reservation.dto.PaymentRequest;
import com.system.hotel_reservation.entity.Booking;
import com.system.hotel_reservation.entity.Payment;
import com.system.hotel_reservation.enums.BookingStatus;
import com.system.hotel_reservation.enums.PaymentStatus;
import com.system.hotel_reservation.repository.BookingRepository;
import com.system.hotel_reservation.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;
 private final BookingRepository bookingRepository;
 private final PaymentSettlement settlement;
 private final BookingService bookingService;

	public PaymentService(PaymentRepository paymentRepository,
			BookingRepository bookingRepository, PaymentSettlement settlement, BookingService bookingService) {
        this.settlement=settlement;this.bookingService=bookingService;
		this.paymentRepository = paymentRepository;
		this.bookingRepository = bookingRepository;
	}

	@org.springframework.transaction.annotation.Transactional
    public Payment createPayment(PaymentRequest request) {
		if (request == null || request.getBookingId() == null) {
			throw new IllegalArgumentException("Booking ID is required");
		}
		if (request.getPaymentMethod() == null) {
			throw new IllegalArgumentException("Payment method is required");
		}

		Booking booking = bookingRepository.findById(request.getBookingId())
				.orElseThrow(() -> new IllegalArgumentException(
						"Booking not found: " + request.getBookingId()));

        com.system.hotel_reservation.security.BookingAccess.require(booking);
        booking=bookingService.lockBooking(booking.getId());
        if(request.getPaymentMethod()!=com.system.hotel_reservation.enums.PaymentMethod.CASH)throw new IllegalArgumentException("Use Paystack checkout for card payments");
        if(booking.getStatus()!=BookingStatus.PENDING||!BookingService.blocks(booking))throw new IllegalArgumentException("Only active pending reservations can receive payment");
		if (booking.getStatus() == BookingStatus.CANCELLED
				|| booking.getStatus() == BookingStatus.CHECKED_OUT) {
			throw new IllegalArgumentException(
					"Payments are not allowed for cancelled or checked-out bookings");
		}

		Payment payment = new Payment();
		payment.setBooking(booking);
		payment.setAmount(booking.getTotalAmount());
		payment.setPaymentMethod(request.getPaymentMethod());
		payment.setStatus(PaymentStatus.PENDING);
		payment.setTransactionReference("PAY-" + UUID.randomUUID());

		return paymentRepository.save(payment);
	}

	public List<Payment> getAllPayments() {
		return paymentRepository.findAll();
	}

	public Optional<Payment> getPaymentById(Long id) {
		return paymentRepository.findById(id).map(p->{com.system.hotel_reservation.security.BookingAccess.require(p.getBooking());return p;});
	}

	public Payment markPaymentSuccessful(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Payment not found: " + paymentId));

        if(payment.getPaymentMethod()!=com.system.hotel_reservation.enums.PaymentMethod.CASH)throw new IllegalArgumentException("Provider payments must be verified with Paystack");
        return settlement.settle(paymentId);
    }
}
