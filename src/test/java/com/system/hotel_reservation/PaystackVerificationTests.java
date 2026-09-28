package com.system.hotel_reservation;
import com.system.hotel_reservation.payment.*;
import com.system.hotel_reservation.service.*;
import com.system.hotel_reservation.repository.*;
import com.system.hotel_reservation.entity.*;
import com.system.hotel_reservation.enums.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.net.InetSocketAddress;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class PaystackVerificationTests {
 HttpServer server;PaymentRepository repo;PaymentSettlement settlement;PaystackService service;Payment payment;String response;
 @BeforeEach void setup()throws Exception{
  repo=mock(PaymentRepository.class);settlement=mock(PaymentSettlement.class);
  Customer customer=new Customer();customer.setEmail("guest@example.test");Booking booking=new Booking();booking.setCustomer(customer);
  payment=new Payment();payment.setId(1L);payment.setBooking(booking);payment.setAmount(new BigDecimal("500.00"));payment.setTransactionReference("PAYSTACK-test");payment.setStatus(PaymentStatus.PENDING);
  when(repo.findByTransactionReference("PAYSTACK-test")).thenReturn(Optional.of(payment));when(settlement.settle(1L)).thenReturn(payment);
  server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.createContext("/transaction/verify/",exchange->{byte[] bytes=response.getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();});server.start();
  service=new PaystackService("http://127.0.0.1:"+server.getAddress().getPort(),"sk_test_local","http://localhost:3100",repo,mock(BookingService.class),settlement);
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("guest@example.test","",List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
  response=json("success",50000,"NGN","test","PAYSTACK-test");
 }
 @AfterEach void stop(){server.stop(0);SecurityContextHolder.clearContext();}
 String json(String state,long amount,String currency,String domain,String reference){return "{\"status\":true,\"data\":{\"status\":\""+state+"\",\"amount\":"+amount+",\"currency\":\""+currency+"\",\"domain\":\""+domain+"\",\"reference\":\""+reference+"\"}}";}
 @Test void validSuccessSettles(){service.verifyPayment("PAYSTACK-test");verify(settlement).settle(1L);}
 @Test void amountCurrencyDomainAndReferenceMustMatch(){for(String bad:List.of(json("success",1,"NGN","test","PAYSTACK-test"),json("success",50000,"USD","test","PAYSTACK-test"),json("success",50000,"NGN","live","PAYSTACK-test"),json("success",50000,"NGN","test","other"))){response=bad;assertThrows(IllegalArgumentException.class,()->service.verifyPayment("PAYSTACK-test"));}verifyNoInteractions(settlement);}
 @Test void pendingDoesNotSettleOrMarkFailed(){response=json("pending",50000,"NGN","test","PAYSTACK-test");assertEquals("pending",service.verifyPayment("PAYSTACK-test").getData().getStatus());verifyNoInteractions(settlement);verify(repo,never()).save(any());}
 @Test void webhookRequiresValidSignatureAndReverifies()throws Exception{String body="{\"event\":\"charge.success\",\"data\":{\"reference\":\"PAYSTACK-test\"}}";assertThrows(IllegalArgumentException.class,()->service.webhook(body,"00"));verifyNoInteractions(settlement);Mac mac=Mac.getInstance("HmacSHA512");mac.init(new SecretKeySpec("sk_test_local".getBytes(StandardCharsets.UTF_8),"HmacSHA512"));service.webhook(body,HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8))));verify(settlement).settle(1L);}
 @Test void liveSecretIsRejected(){var live=new PaystackService("http://127.0.0.1:1","test-paystack-secret","http://localhost",repo,mock(BookingService.class),settlement);assertThrows(IllegalArgumentException.class,()->live.verifyPayment("PAYSTACK-test"));verifyNoInteractions(settlement);}
}
