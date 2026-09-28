package com.system.hotel_reservation.service;

import com.system.hotel_reservation.entity.Room;
import com.system.hotel_reservation.enums.BookingStatus;
import com.system.hotel_reservation.repository.BookingRepository;
import com.system.hotel_reservation.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomService {

	private final RoomRepository roomRepository;
	private final BookingRepository bookingRepository;

	public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
		this.roomRepository = roomRepository;
		this.bookingRepository = bookingRepository;
	}

	public Room createRoom(Room room) {
        validate(room);
		return roomRepository.save(room);
	}

	public List<Room> getAllRooms() {
		return roomRepository.findAll();
	}

	public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
		if (checkInDate == null || checkOutDate == null
				|| !checkOutDate.isAfter(checkInDate)) {
			throw new IllegalArgumentException("Check-out date must be after check-in date");
		}

		Set<BookingStatus> activeStatuses = Set.of(
				BookingStatus.PENDING,
				BookingStatus.CONFIRMED,
				BookingStatus.CHECKED_IN);

		if(checkInDate.isBefore(BookingService.today())) throw new IllegalArgumentException("Check-in cannot be in the past");
        return roomRepository.findAll().stream()
            .filter(room -> room.getStatus() != com.system.hotel_reservation.enums.RoomStatus.MAINTENANCE)
            .filter(room -> room.getStatus() != com.system.hotel_reservation.enums.RoomStatus.OCCUPIED || checkInDate.isAfter(BookingService.today()))
            .filter(room -> bookingRepository.findByRoomId(room.getId()).stream().noneMatch(b -> BookingService.blocks(b) && b.getCheckInDate().isBefore(checkOutDate) && b.getCheckOutDate().isAfter(checkInDate)))
            .collect(Collectors.toList());
	}

	public Optional<Room> getRoomById(Long id) {
		return roomRepository.findById(id);
	}

	@org.springframework.transaction.annotation.Transactional
    public Room updateRoom(Long id, Room room) {
		Room current=roomRepository.lockById(id).orElseThrow(()->new IllegalArgumentException("Room not found"));
        if(current.getStatus()==com.system.hotel_reservation.enums.RoomStatus.OCCUPIED && room.getStatus()!=current.getStatus()) throw new IllegalArgumentException("Check out the guest before changing occupancy");
        validate(room);
        room.setId(id);
		return roomRepository.save(room);
	}

    private void validate(Room r){if(r.getRoomNumber()==null||r.getRoomNumber().isBlank()||r.getRoomType()==null||r.getStatus()==null||r.getPricePerNight()==null||r.getPricePerNight().signum()<=0||r.getMaxGuests()<1||r.getMaxGuests()>12)throw new IllegalArgumentException("Valid room number, type, status, price and capacity are required");}
	public void deleteRoom(Long id) {
		roomRepository.deleteById(id);
	}
}
