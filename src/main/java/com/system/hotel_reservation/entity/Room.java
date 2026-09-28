package com.system.hotel_reservation.entity;

import com.system.hotel_reservation.enums.RoomStatus;
import com.system.hotel_reservation.enums.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String roomNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RoomType roomType;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal pricePerNight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RoomStatus status;

	@Column(length = 1000)
	private String description;

 private Integer maxGuests;
 public int getMaxGuests(){return maxGuests!=null ? maxGuests : roomType==null ? 1 : switch(roomType){case SINGLE -> 1;case DOUBLE -> 2;case DELUXE -> 3;case SUITE -> 4;};}
 public void setMaxGuests(Integer value){maxGuests=value;}
	public Room() {
	}

	public Room(Long id, String roomNumber, RoomType roomType, BigDecimal pricePerNight,
			RoomStatus status, String description) {
		this.id = id;
		this.roomNumber = roomNumber;
		this.roomType = roomType;
		this.pricePerNight = pricePerNight;
		this.status = status;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
