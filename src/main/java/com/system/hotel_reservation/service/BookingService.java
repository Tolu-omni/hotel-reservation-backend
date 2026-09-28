package com.system.hotel_reservation.service;
import com.system.hotel_reservation.dto.BookingRequest;
import com.system.hotel_reservation.entity.*;
import com.system.hotel_reservation.enums.*;
import com.system.hotel_reservation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class BookingService {
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;
 private final BookingRepository bookings; private final RoomRepository rooms; private final CustomerRepository customers; private final UserRepository users; private final PaymentRepository payments;
 public BookingService(BookingRepository b, CustomerRepository c, RoomRepository r, UserRepository u, PaymentRepository p){bookings=b;customers=c;rooms=r;users=u;payments=p;}
 public static LocalDate today(){return LocalDate.now(ZoneId.of("Africa/Lagos"));}
 public static LocalDateTime now(){return LocalDateTime.now(ZoneId.of("Africa/Lagos"));}
 public static boolean blocks(Booking b){return b.getStatus()==BookingStatus.CONFIRMED||b.getStatus()==BookingStatus.CHECKED_IN||(b.getStatus()==BookingStatus.PENDING&&(b.getHoldExpiresAt()==null||b.getHoldExpiresAt().isAfter(now())));}
 private static ResponseStatusException conflict(String message){return new ResponseStatusException(HttpStatus.CONFLICT,message);}
 private Room lockRoom(Long id){if(id==null)throw new IllegalArgumentException("Room is required");return rooms.lockById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Room not found"));}
 private Booking find(Long id){return bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found"));}
 public Booking lockBooking(Long id){Booking b=find(id);lockRoom(b.getRoom().getId());em.refresh(b);return b;}
 private void validate(Room room,BookingRequest req,Long exclude){
  if(req.getCheckInDate()==null||req.getCheckOutDate()==null||req.getCheckInDate().isBefore(today())||!req.getCheckOutDate().isAfter(req.getCheckInDate()))throw new IllegalArgumentException("Choose check-in today or later and checkout after check-in");
  if(req.getGuestCount()==null||req.getGuestCount()<1||req.getGuestCount()>room.getMaxGuests())throw new IllegalArgumentException("Guest count exceeds room capacity");
  if(room.getStatus()==RoomStatus.OCCUPIED&&!req.getCheckInDate().isAfter(today()))throw conflict("Room is currently occupied");
  if(room.getStatus()==RoomStatus.MAINTENANCE)throw conflict("Room is under maintenance");
  if(room.getPricePerNight()==null||room.getPricePerNight().signum()<=0)throw new IllegalArgumentException("Room price is not configured");
  if(req.getSpecialRequests()!=null&&req.getSpecialRequests().length()>2000)throw new IllegalArgumentException("Special requests must be at most 2000 characters");
  var active=bookings.lockOverlaps(room.getId(),Set.of(BookingStatus.PENDING,BookingStatus.CONFIRMED,BookingStatus.CHECKED_IN),req.getCheckInDate(),req.getCheckOutDate());
  if(active.stream().anyMatch(b->!Objects.equals(b.getId(),exclude)&&blocks(b)))throw conflict("Room is unavailable for these dates. Please choose another room or date range.");
 }
 private void apply(Booking b,Room room,BookingRequest r){b.setRoom(room);b.setCheckInDate(r.getCheckInDate());b.setCheckOutDate(r.getCheckOutDate());b.setGuestCount(r.getGuestCount());b.setSpecialRequests(r.getSpecialRequests());b.setTotalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(r.getCheckInDate(),r.getCheckOutDate()))));}
 public Booking createBooking(BookingRequest r){return createBooking(r,null);}
 public Booking createBooking(BookingRequest r,String email){
  if(r==null)throw new IllegalArgumentException("Booking is required");Room room=lockRoom(r.getRoomId());validate(room,r,null);
  Customer c;
  if(email!=null){c=customers.findByEmail(email).orElseGet(()->{User u=users.findByEmail(email).orElseThrow();Customer n=new Customer();n.setFirstName(u.getFirstName());n.setLastName(u.getLastName());n.setEmail(u.getEmail());return customers.save(n);});}
  else {if(r.getCustomerId()==null)throw new IllegalArgumentException("Select a customer");c=customers.findById(r.getCustomerId()).orElseThrow(()->new IllegalArgumentException("Customer not found"));}
  Booking b=new Booking();b.setCustomer(c);apply(b,room,r);b.setStatus(BookingStatus.PENDING);b.setHoldExpiresAt(now().plusMinutes(30));return bookings.save(b);
 }
 public Booking updateBooking(Long id,BookingRequest r){
  Booking b=lockBooking(id);if(b.getStatus()!=BookingStatus.PENDING)throw conflict("Only unpaid pending reservations can be edited");
  if(payments.existsByBookingIdAndStatus(id,PaymentStatus.SUCCESSFUL))throw conflict("Paid reservations require reception assistance");
  if(r.getRoomId()==null)r.setRoomId(b.getRoom().getId());
  if(!r.getRoomId().equals(b.getRoom().getId()))throw conflict("Cancel this unpaid reservation and create a new one to change rooms");
  if(r.getCheckInDate()==null)r.setCheckInDate(b.getCheckInDate());if(r.getCheckOutDate()==null)r.setCheckOutDate(b.getCheckOutDate());if(r.getGuestCount()==null)r.setGuestCount(b.getGuestCount());if(r.getSpecialRequests()==null)r.setSpecialRequests(b.getSpecialRequests());
  validate(b.getRoom(),r,id);apply(b,b.getRoom(),r);return bookings.save(b);
 }
 public List<Booking> getAllBookings(){return bookings.findAll();}
 public Optional<Booking> getBookingById(Long id){return bookings.findById(id);}
 public Optional<Booking> getBookingForCustomer(Long id,String email){return bookings.findById(id).filter(b->b.getCustomer().getEmail().equalsIgnoreCase(email));}
 public List<Booking> getMyBookings(String email){return customers.findByEmail(email).map(c->bookings.findByCustomerId(c.getId())).orElse(List.of());}
 public Booking cancelBooking(Long id){Booking b=lockBooking(id);if(b.getStatus()!=BookingStatus.PENDING&&b.getStatus()!=BookingStatus.CONFIRMED)throw conflict("Only pending or confirmed reservations can be cancelled");if(payments.existsByBookingIdAndStatus(id,PaymentStatus.SUCCESSFUL))throw conflict("Paid cancellation requires refund reconciliation; contact management");b.setStatus(BookingStatus.CANCELLED);return bookings.save(b);}
 public Booking confirmBooking(Long id){Booking b=lockBooking(id);if(b.getStatus()!=BookingStatus.PENDING||!blocks(b))throw conflict("This reservation is no longer pending or its hold expired");if(!payments.existsByBookingIdAndStatus(id,PaymentStatus.SUCCESSFUL))throw conflict("Verify payment or record cash before confirming");
  var overlaps=bookings.lockOverlaps(b.getRoom().getId(),Set.of(BookingStatus.PENDING,BookingStatus.CONFIRMED,BookingStatus.CHECKED_IN),b.getCheckInDate(),b.getCheckOutDate());
  if(b.getRoom().getStatus()==RoomStatus.MAINTENANCE||overlaps.stream().anyMatch(x->!x.getId().equals(id)&&blocks(x)))throw conflict("Room requires reconciliation before confirmation");b.setStatus(BookingStatus.CONFIRMED);return bookings.save(b);}
 public Booking checkIn(Long id){Booking b=lockBooking(id);if(b.getStatus()!=BookingStatus.CONFIRMED||b.getCheckInDate().isAfter(today())||!b.getCheckOutDate().isAfter(today()))throw conflict("Check-in is allowed only during the confirmed stay dates");if(b.getRoom().getStatus()!=RoomStatus.AVAILABLE)throw conflict("Room must be available before check-in");b.setStatus(BookingStatus.CHECKED_IN);b.getRoom().setStatus(RoomStatus.OCCUPIED);rooms.save(b.getRoom());return bookings.save(b);}
 public Booking checkOut(Long id){Booking b=lockBooking(id);if(b.getStatus()!=BookingStatus.CHECKED_IN)throw conflict("Only checked-in stays can check out");b.setStatus(BookingStatus.CHECKED_OUT);if(b.getRoom().getStatus()==RoomStatus.OCCUPIED)b.getRoom().setStatus(RoomStatus.AVAILABLE);rooms.save(b.getRoom());return bookings.save(b);}
 public void deleteBooking(Long id){throw conflict("Reservation history is retained. Cancel an eligible reservation instead.");}
}
