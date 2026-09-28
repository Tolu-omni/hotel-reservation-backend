package com.system.hotel_reservation.payment;
import com.system.hotel_reservation.entity.*;
import com.system.hotel_reservation.enums.*;
import com.system.hotel_reservation.repository.*;
import com.system.hotel_reservation.service.*;
import com.system.hotel_reservation.security.BookingAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
@Service
public class PaystackService {
 private final RestClient client;private final String secret;private final String frontend;private final PaymentRepository payments;private final BookingService bookings;private final PaymentSettlement settlement;
 public PaystackService(@Value("${paystack.base-url}")String base,@Value("${paystack.secret-key}")String key,@Value("${frontend.base-url:http://localhost:3000}")String front,PaymentRepository p,BookingService b,PaymentSettlement s){var factory=new org.springframework.http.client.SimpleClientHttpRequestFactory();factory.setConnectTimeout(5000);factory.setReadTimeout(15000);client=RestClient.builder().requestFactory(factory).baseUrl(base).build();secret=key;frontend=front;payments=p;bookings=b;settlement=s;}
 private void testOnly(){if(secret==null||!secret.startsWith("sk_test_"))throw new IllegalArgumentException("Configure a Paystack TEST secret. Live payments are disabled in this presentation build.");}
 @Transactional public PaystackResponse initializePayment(Long bookingId){
  testOnly();Booking b=bookings.lockBooking(bookingId);BookingAccess.require(b);
  if(b.getStatus()!=BookingStatus.PENDING||!BookingService.blocks(b))throw new IllegalArgumentException("This reservation is not payable or the 30-minute hold expired");
  if(payments.existsByBookingIdAndStatus(bookingId,PaymentStatus.SUCCESSFUL))throw new IllegalArgumentException("Payment already recorded");
  Payment p=payments.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(bookingId,PaymentStatus.PENDING).filter(x->x.getPaymentMethod()==PaymentMethod.CARD&&x.getTransactionReference().startsWith("PAYSTACK-")).orElse(null);
  if(p!=null&&p.getAuthorizationUrl()!=null)return new PaystackResponse(true,"Continue test checkout",new PaystackResponse.Data(p.getAuthorizationUrl(),p.getAccessCode(),p.getTransactionReference(),"pending",null));
  if(p!=null)throw new IllegalArgumentException("An existing checkout needs verification before retrying");
  p=new Payment();p.setBooking(b);p.setAmount(b.getTotalAmount());p.setStatus(PaymentStatus.PENDING);p.setPaymentMethod(PaymentMethod.CARD);p.setTransactionReference("PAYSTACK-"+UUID.randomUUID());payments.save(p);
  var response=client.post().uri("/transaction/initialize").contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+secret).body(Map.of("email",b.getCustomer().getEmail(),"amount",p.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact(),"currency","NGN","reference",p.getTransactionReference(),"callback_url",frontend.replaceAll("/$","")+"/payment/verify")).retrieve().body(PaystackResponse.class);
  if(response==null||!response.isStatus()||response.getData()==null||!p.getTransactionReference().equals(response.getData().getReference())||response.getData().getAuthorizationUrl()==null)throw new IllegalArgumentException("Invalid Paystack initialization response");
  p.setAuthorizationUrl(response.getData().getAuthorizationUrl());p.setAccessCode(response.getData().getAccessCode());payments.save(p);return response;
 }
 public PaystackResponse verifyPayment(String reference){Payment p=find(reference);BookingAccess.require(p.getBooking());return verify(p);}
 private Payment find(String ref){return payments.findByTransactionReference(ref).orElseThrow(()->new IllegalArgumentException("Payment reference not found"));}
 private PaystackResponse verify(Payment p){
  testOnly();
  if(p.getStatus()==PaymentStatus.SUCCESSFUL)return new PaystackResponse(true,"Payment recorded",new PaystackResponse.Data(null,null,p.getTransactionReference(),p.isReconciliationRequired()?"reconciliation_required":"success",null));
  var response=client.get().uri("/transaction/verify/{reference}",p.getTransactionReference()).header("Authorization","Bearer "+secret).retrieve().body(PaystackResponse.class);
  if(response==null||!response.isStatus()||response.getData()==null)throw new IllegalArgumentException("Payment verification is temporarily unavailable");
  var d=response.getData();
  if(!p.getTransactionReference().equals(d.getReference())||d.getAmount()==null||d.getAmount()!=p.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact()||!"NGN".equals(d.getCurrency())||!"test".equals(d.getDomain()))throw new IllegalArgumentException("Payment details do not match the reservation");
  if("success".equals(d.getStatus())){Payment saved=settlement.settle(p.getId());if(saved.isReconciliationRequired())d.setStatus("reconciliation_required");}
  // Non-success outcomes are retryable; never overwrite a concurrently settled payment.
  return response;
 }
 public void webhook(String body,String signature){
  testOnly();
  try{Mac mac=Mac.getInstance("HmacSHA512");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA512"));byte[] expected=mac.doFinal(body.getBytes(StandardCharsets.UTF_8));byte[] supplied=HexFormat.of().parseHex(signature==null?"":signature);if(!MessageDigest.isEqual(expected,supplied))throw new IllegalArgumentException("Invalid webhook signature");
   var json=new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);if("charge.success".equals(json.path("event").asText()))verify(find(json.path("data").path("reference").asText()));
  }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalArgumentException("Webhook could not be processed",e);}
 }
}
