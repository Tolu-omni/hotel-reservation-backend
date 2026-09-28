package com.system.hotel_reservation.dto;

import com.system.hotel_reservation.enums.PaymentMethod;

public class PaymentRequest {

	private Long bookingId;
	private PaymentMethod paymentMethod;

	public PaymentRequest() {
	}

	public PaymentRequest(Long bookingId, PaymentMethod paymentMethod) {
		this.bookingId = bookingId;
		this.paymentMethod = paymentMethod;
	}

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}
