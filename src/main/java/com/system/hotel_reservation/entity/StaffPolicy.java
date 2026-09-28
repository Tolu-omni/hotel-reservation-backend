package com.system.hotel_reservation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff_policy")
public class StaffPolicy {

	@Id
	private Long id = 1L;

	private boolean canAnswerCalls = true;
	private boolean canViewGuests = true;
	private boolean canCheckIn = true;
	private boolean canEditBookings = true;
	private boolean canTakePayments = true;

	public StaffPolicy() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isCanAnswerCalls() {
		return canAnswerCalls;
	}

	public void setCanAnswerCalls(boolean canAnswerCalls) {
		this.canAnswerCalls = canAnswerCalls;
	}

	public boolean isCanViewGuests() {
		return canViewGuests;
	}

	public void setCanViewGuests(boolean canViewGuests) {
		this.canViewGuests = canViewGuests;
	}

	public boolean isCanCheckIn() {
		return canCheckIn;
	}

	public void setCanCheckIn(boolean canCheckIn) {
		this.canCheckIn = canCheckIn;
	}

	public boolean isCanEditBookings() {
		return canEditBookings;
	}

	public void setCanEditBookings(boolean canEditBookings) {
		this.canEditBookings = canEditBookings;
	}

	public boolean isCanTakePayments() {
		return canTakePayments;
	}

	public void setCanTakePayments(boolean canTakePayments) {
		this.canTakePayments = canTakePayments;
	}
}
