package com.system.hotel_reservation.service;
import com.system.hotel_reservation.entity.*;
import com.system.hotel_reservation.repository.MailOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.*;
import org.springframework.transaction.annotation.Transactional;
@Service @EnableScheduling
public class MailOutboxService {
 private final MailOutboxRepository repo;private final ObjectProvider<JavaMailSender> sender;private final String mode;private final String from;
 public MailOutboxService(MailOutboxRepository r,ObjectProvider<JavaMailSender> s,@Value("${nova.mail-mode:capture}")String m,@Value("${nova.mail-from:noreply@example.invalid}")String f){repo=r;sender=s;mode=m;from=f;}
 @Transactional public void confirmation(Booking b){
  String key="confirmed:"+b.getId();if(repo.existsByEventKey(key))return;
  MailOutbox m=new MailOutbox();m.eventKey=key;m.recipient=b.getCustomer().getEmail();m.subject="[TEST] Nova Suites reservation #"+b.getId();m.body="TEST RESERVATION — no live payment\n\nHello "+b.getCustomer().getFirstName()+",\nYour reservation is confirmed.\nRoom: "+b.getRoom().getRoomNumber()+"\nDates: "+b.getCheckInDate()+" to "+b.getCheckOutDate()+"\nGuests: "+b.getGuestCount()+"\nAmount: NGN "+b.getTotalAmount()+"\n\nView your booking in Nova Suites. Contact reception for any changes.";
  m.status="smtp".equals(mode)?"QUEUED":"CAPTURED";m.createdAt=BookingService.now();repo.save(m);
 }
 @Scheduled(fixedDelay=60000) @Transactional public void deliver(){
  if(!"smtp".equals(mode))return;
  for(MailOutbox row:repo.pending(org.springframework.data.domain.PageRequest.of(0,10))){
   row.attempts++;try{JavaMailSender transport=sender.getIfAvailable();if(transport==null)throw new IllegalStateException("SMTP not configured");SimpleMailMessage message=new SimpleMailMessage();message.setFrom(from);message.setTo(row.recipient);message.setSubject(row.subject);message.setText(row.body);transport.send(message);row.status="SENT";row.sentAt=BookingService.now();}catch(Exception ignored){if(row.attempts>=5)row.status="FAILED";}repo.save(row);
  }
 }
}
