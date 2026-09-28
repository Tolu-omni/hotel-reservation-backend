package com.system.hotel_reservation.service;
import com.system.hotel_reservation.entity.*;
import com.system.hotel_reservation.enums.*;
import com.system.hotel_reservation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
@Service
public class PaymentSettlement {
 private final PaymentRepository payments;private final BookingRepository bookings;private final BookingService service;private final MailOutboxService mail;
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;
 public PaymentSettlement(PaymentRepository p,BookingRepository b,BookingService s,MailOutboxService m){payments=p;bookings=b;service=s;mail=m;}
 @Transactional public Payment settle(Long id){
  Payment p=payments.findById(id).orElseThrow();Booking b=service.lockBooking(p.getBooking().getId());em.refresh(p);
  if(p.getStatus()==PaymentStatus.SUCCESSFUL)return p;
  boolean duplicate=payments.existsByBookingIdAndStatus(b.getId(),PaymentStatus.SUCCESSFUL);
  boolean eligible=b.getStatus()==BookingStatus.PENDING&&BookingService.blocks(b)&&!b.getCheckInDate().isBefore(BookingService.today())&&b.getRoom().getStatus()!=RoomStatus.MAINTENANCE;
  if(eligible)eligible=bookings.lockOverlaps(b.getRoom().getId(),Set.of(BookingStatus.PENDING,BookingStatus.CONFIRMED,BookingStatus.CHECKED_IN),b.getCheckInDate(),b.getCheckOutDate()).stream().noneMatch(x->!x.getId().equals(b.getId())&&BookingService.blocks(x));
  var actor=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
  p.setSettledBy(actor==null?"provider-webhook":actor.getName());
  p.setStatus(PaymentStatus.SUCCESSFUL);p.setPaidAt(BookingService.now());p.setReconciliationRequired(duplicate||!eligible);
  if(!p.isReconciliationRequired()){b.setStatus(BookingStatus.CONFIRMED);bookings.save(b);mail.confirmation(b);}
  return payments.save(p);
 }
}
