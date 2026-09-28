package com.system.hotel_reservation.repository;
import com.system.hotel_reservation.entity.MailOutbox;
import java.util.List;
public interface MailOutboxRepository extends org.springframework.data.jpa.repository.JpaRepository<MailOutbox,Long>{
 boolean existsByEventKey(String key);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select m from MailOutbox m where m.status = 'QUEUED' and m.attempts < 5 order by m.id")
 List<MailOutbox> pending(org.springframework.data.domain.Pageable page);
}
