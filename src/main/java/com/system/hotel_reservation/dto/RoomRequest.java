package com.system.hotel_reservation.dto;

import com.system.hotel_reservation.enums.RoomStatus;
import com.system.hotel_reservation.enums.RoomType;

import java.math.BigDecimal;

public class RoomRequest {

	private String roomNumber;
	private RoomType roomType;
	private BigDecimal pricePerNight;
	private RoomStatus status;
	private String description;

	public RoomRequest() {
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public RoomType getRoomType() {
		return roomType;
	}

	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	public BigDecimal getPricePerNight() {
		return pricePerNight;
	}

	public void setPricePerNight(BigDecimal pricePerNight) {
		this.pricePerNight = pricePerNight;
	}

	public RoomStatus getStatus() {
		return status;
	}

	public void setStatus(RoomStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
