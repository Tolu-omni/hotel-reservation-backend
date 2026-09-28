package com.system.hotel_reservation.controller;

import com.system.hotel_reservation.entity.Room;
import com.system.hotel_reservation.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@PostMapping
	public ResponseEntity<Room> createRoom(@RequestBody Room room) {
		return ResponseEntity.ok(roomService.createRoom(room));
	}

	@GetMapping
	public ResponseEntity<List<Room>> getAllRooms() {
		return ResponseEntity.ok(roomService.getAllRooms());
	}

	@GetMapping("/available")
	public ResponseEntity<List<Room>> getAvailableRooms(
			@RequestParam LocalDate checkInDate,
			@RequestParam LocalDate checkOutDate) {
		return ResponseEntity.ok(roomService.getAvailableRooms(checkInDate, checkOutDate));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
		return ResponseEntity.of(roomService.getRoomById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
		return ResponseEntity.ok(roomService.updateRoom(id, room));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
		roomService.deleteRoom(id);
		return ResponseEntity.noContent().build();
	}
}
