package com.system.hotel_reservation;

import com.system.hotel_reservation.dto.BookingRequest;
import com.system.hotel_reservation.entity.*;
import com.system.hotel_reservation.enums.*;
import com.system.hotel_reservation.repository.*;
import com.system.hotel_reservation.service.*;
import com.system.hotel_reservation.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class NovaWorkflowTests {
 @Autowired BookingService service; @Autowired RoomService roomService; @Autowired CustomerService customerService;
 @Autowired BookingRepository bookings; @Autowired RoomRepository rooms; @Autowired CustomerRepository customers;
 @Autowired PaymentRepository payments; @Autowired PaymentSettlement settlement; @Autowired MailOutboxRepository mail;
 @Autowired UserRepository users; @Autowired JwtService jwt;
 @Value("${local.server.port}") int port;
 Room room;Customer customer;
 @BeforeEach void fixture(){
  mail.deleteAll();payments.deleteAll();bookings.deleteAll();rooms.deleteAll();customers.deleteAll();users.deleteAll();
  room=rooms.save(new Room(null,"101",RoomType.DOUBLE,new BigDecimal("50000"),RoomStatus.AVAILABLE,"Test room"));
  customer=new Customer();customer.setFirstName("Demo");customer.setLastName("Guest");customer.setEmail("guest@example.test");customer.setPhoneNumber("08000000000");customer.setAddress("Original address");customer=customers.save(customer);
 }
 BookingRequest request(LocalDate start,LocalDate end){BookingRequest r=new BookingRequest();r.setRoomId(room.getId());r.setCustomerId(customer.getId());r.setCheckInDate(start);r.setCheckOutDate(end);r.setGuestCount(2);return r;}
 Booking reserve(){return service.createBooking(request(BookingService.today(),BookingService.today().plusDays(2)));}
 Payment pending(Booking b){Payment p=new Payment();p.setBooking(b);p.setAmount(b.getTotalAmount());p.setPaymentMethod(PaymentMethod.CARD);p.setStatus(PaymentStatus.PENDING);p.setTransactionReference("PAYSTACK-"+UUID.randomUUID());return payments.save(p);}
 @Test void overlappingRejectedAdjacentAllowed(){reserve();assertThrows(RuntimeException.class,()->reserve());assertDoesNotThrow(()->service.createBooking(request(BookingService.today().plusDays(2),BookingService.today().plusDays(3))));}
 @Test void concurrentRequestsHaveOneWinner()throws Exception{var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);Callable<Boolean> task=()->{start.await();try{reserve();return true;}catch(RuntimeException e){return false;}};var a=pool.submit(task);var b=pool.submit(task);start.countDown();assertEquals(1,(a.get(15,TimeUnit.SECONDS)?1:0)+(b.get(15,TimeUnit.SECONDS)?1:0));pool.shutdownNow();assertEquals(1,bookings.count());}
 @Test void maintenanceCapacityAndPastDatesRejected(){room.setStatus(RoomStatus.MAINTENANCE);rooms.save(room);assertThrows(RuntimeException.class,()->reserve());assertTrue(roomService.getAvailableRooms(BookingService.today(),BookingService.today().plusDays(1)).isEmpty());room.setStatus(RoomStatus.AVAILABLE);rooms.save(room);BookingRequest r=request(BookingService.today(),BookingService.today().plusDays(1));r.setGuestCount(9);assertThrows(RuntimeException.class,()->service.createBooking(r));assertThrows(RuntimeException.class,()->service.createBooking(request(BookingService.today().minusDays(1),BookingService.today())));}
 @Test void expiredHoldReleasesRoomAndLatePaymentNeedsReconciliation(){Booking b=reserve();b.setHoldExpiresAt(BookingService.now().minusMinutes(1));bookings.save(b);assertEquals(1,roomService.getAvailableRooms(b.getCheckInDate(),b.getCheckOutDate()).size());reserve();Payment p=settlement.settle(pending(b).getId());assertTrue(p.isReconciliationRequired());assertEquals(BookingStatus.PENDING,bookings.findById(b.getId()).orElseThrow().getStatus());assertEquals(0,mail.count());}
 @Test void settlementIdempotentAndOccupancyFollowsCheckin(){Booking b=reserve();Payment p=pending(b);assertFalse(settlement.settle(p.getId()).isReconciliationRequired());settlement.settle(p.getId());assertEquals(1,mail.count());assertEquals("CAPTURED",mail.findAll().getFirst().status);assertEquals(BookingStatus.CONFIRMED,bookings.findById(b.getId()).orElseThrow().getStatus());assertEquals(RoomStatus.AVAILABLE,rooms.findById(room.getId()).orElseThrow().getStatus());service.checkIn(b.getId());assertEquals(RoomStatus.OCCUPIED,rooms.findById(room.getId()).orElseThrow().getStatus());assertThrows(RuntimeException.class,()->service.cancelBooking(b.getId()));service.checkOut(b.getId());assertEquals(RoomStatus.AVAILABLE,rooms.findById(room.getId()).orElseThrow().getStatus());}
 @Test void editPreservesCustomerAndRecalculatesAndRejectsOverlap(){Booking b=reserve();BookingRequest r=request(BookingService.today(),BookingService.today().plusDays(3));r.setSpecialRequests("Quiet room");Booking changed=service.updateBooking(b.getId(),r);assertEquals(customer.getId(),changed.getCustomer().getId());assertEquals(new BigDecimal("150000.00"),changed.getTotalAmount().setScale(2));assertEquals("Quiet room",changed.getSpecialRequests());service.createBooking(request(BookingService.today().plusDays(3),BookingService.today().plusDays(4)));r.setCheckOutDate(BookingService.today().plusDays(4));assertThrows(RuntimeException.class,()->service.updateBooking(b.getId(),r));}
 @Test void customerUpdatePreservesFieldsAndEmail(){Customer patch=new Customer();patch.setFirstName("Updated");Customer result=customerService.updateCustomer(customer.getId(),patch);assertEquals("Original address",result.getAddress());assertEquals("08000000000",result.getPhoneNumber());patch.setEmail("someoneelse@example.test");assertThrows(RuntimeException.class,()->customerService.updateCustomer(customer.getId(),patch));}
 String token(String email,Role role){User u=new User();u.setEmail(email);u.setPassword("unused-test-hash");u.setRole(role);u.setFirstName("Test");u.setLastName("User");users.save(u);return jwt.generateToken(email,role.name());}
 HttpResponse<String> call(String method,String path,String body,String token)throws Exception{var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(body));if(token!=null)b.header("Authorization","Bearer "+token);return HttpClient.newHttpClient().send(b.build(),HttpResponse.BodyHandlers.ofString());}
 @Test void anonymousMutationsAndCrossCustomerPaymentDenied()throws Exception{Booking b=reserve();Payment p=pending(b);for(String method:List.of("PUT","DELETE"))assertTrue(Set.of(401,403).contains(call(method,"/api/bookings/"+b.getId(),"{}",null).statusCode()));assertTrue(Set.of(401,403).contains(call("POST","/api/customers","{}",null).statusCode()));String other=token("other@example.test",Role.CUSTOMER);assertEquals(403,call("GET","/api/payments/"+p.getId(),"",other).statusCode());assertEquals(403,call("POST","/api/payments/initialize/"+b.getId(),"{}",other).statusCode());assertEquals(403,call("GET","/api/payments/verify/"+p.getTransactionReference(),"",other).statusCode());assertEquals(403,call("GET","/api/customers","",other).statusCode());}
 @Test void contactPersistsAndCannotBeReadAnonymously()throws Exception{var result=call("POST","/api/contact","{\"name\":\"Demo Guest\",\"email\":\"demo@example.test\",\"subject\":\"Stay\",\"message\":\"A test inquiry about a stay\"}",null);assertEquals(200,result.statusCode(),result.body());assertTrue(Set.of(401,403).contains(call("GET","/api/contact","",null).statusCode()));assertEquals(400,call("POST","/api/contact","{}",null).statusCode());}
}
