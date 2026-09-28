package com.system.hotel_reservation.repository;

import com.system.hotel_reservation.entity.Payment;
import com.system.hotel_reservation.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByBookingId(Long bookingId);

	boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);

	Optional<Payment> findByTransactionReference(String transactionReference);

	Optional<Payment> findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
			Long bookingId, PaymentStatus status);
}
