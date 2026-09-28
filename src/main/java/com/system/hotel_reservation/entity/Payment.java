package com.system.hotel_reservation.entity;

import com.system.hotel_reservation.enums.PaymentMethod;
import com.system.hotel_reservation.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(nullable = false, unique = true)
	private String transactionReference;

 private LocalDateTime paidAt;
 @Column(length=1000) private String authorizationUrl;
 private String accessCode;
 private boolean reconciliationRequired;
 private String settledBy;
 public String getSettledBy(){return settledBy;}
 public void setSettledBy(String value){settledBy=value;}
 public String getAuthorizationUrl(){return authorizationUrl;}
 public void setAuthorizationUrl(String value){authorizationUrl=value;}
 public String getAccessCode(){return accessCode;}
 public void setAccessCode(String value){accessCode=value;}
 public boolean isReconciliationRequired(){return reconciliationRequired;}
 public void setReconciliationRequired(boolean value){reconciliationRequired=value;}


	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Payment() {
	}

	public Payment(Long id, Booking booking, BigDecimal amount,
			PaymentMethod paymentMethod, PaymentStatus status,
			String transactionReference, LocalDateTime paidAt,
			LocalDateTime createdAt) {
		this.id = id;
		this.booking = booking;
		this.amount = amount;
		this.paymentMethod = paymentMethod;
		this.status = status;
		this.transactionReference = transactionReference;
		this.paidAt = paidAt;
		this.createdAt = createdAt;
	}

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Booking getBooking() {
		return booking;
	}

	public void setBooking(Booking booking) {
		this.booking = booking;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(String transactionReference) {
		this.transactionReference = transactionReference;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
