package com.system.hotel_reservation.repository;

import com.system.hotel_reservation.entity.Booking;
import com.system.hotel_reservation.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findByCustomerId(Long customerId);

	@Query("""
			SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
			FROM Booking b
			WHERE b.room.id = :roomId
			  AND b.status IN :statuses
			  AND b.checkInDate < :requestedCheckOutDate
			  AND b.checkOutDate > :requestedCheckInDate
			""")
	boolean existsOverlappingBooking(
			@Param("roomId") Long roomId,
			@Param("statuses") Collection<BookingStatus> statuses,
			@Param("requestedCheckInDate") LocalDate requestedCheckInDate,
			@Param("requestedCheckOutDate") LocalDate requestedCheckOutDate);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("select b from Booking b where b.room.id = :roomId and b.status in :statuses and b.checkInDate < :end and b.checkOutDate > :start")
 List<Booking> lockOverlaps(@Param("roomId") Long roomId, @Param("statuses") Collection<BookingStatus> statuses, @Param("start") LocalDate start, @Param("end") LocalDate end);
 List<Booking> findByRoomId(Long roomId);
}
