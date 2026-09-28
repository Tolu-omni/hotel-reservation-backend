package com.system.hotel_reservation.repository;

import com.system.hotel_reservation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select r from Room r where r.id = :id")
 java.util.Optional<Room> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
