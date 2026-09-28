package com.system.hotel_reservation.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="mail_outbox")
public class MailOutbox {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(unique=true,nullable=false) public String eventKey;
 public String recipient;public String subject;
 @Column(length=8000) public String body;
 public String status;public int attempts;public LocalDateTime createdAt;public LocalDateTime sentAt;
}
