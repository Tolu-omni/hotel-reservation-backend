package com.system.hotel_reservation.dto;

import java.time.LocalDate;

public class BookingRequest {

	private Long customerId;
	private Long roomId;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private Integer guestCount;

 private String specialRequests;
 public String getSpecialRequests(){return specialRequests;}
 public void setSpecialRequests(String value){specialRequests=value;}

	public BookingRequest() {
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public LocalDate getCheckInDate() {
		return checkInDate;
	}

	public void setCheckInDate(LocalDate checkInDate) {
		this.checkInDate = checkInDate;
	}

	public LocalDate getCheckOutDate() {
		return checkOutDate;
	}

	public void setCheckOutDate(LocalDate checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	public Integer getGuestCount() {
		return guestCount;
	}

	public void setGuestCount(Integer guestCount) {
		this.guestCount = guestCount;
	}
}
