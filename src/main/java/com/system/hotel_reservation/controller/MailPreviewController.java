package com.system.hotel_reservation.controller;
import org.springframework.web.bind.annotation.*;
import com.system.hotel_reservation.repository.MailOutboxRepository;
import com.system.hotel_reservation.entity.MailOutbox;
import java.util.List;
@RestController
public class MailPreviewController {
 private final MailOutboxRepository repo;
 public MailPreviewController(MailOutboxRepository r){repo=r;}
 @GetMapping("/api/mail-preview") public List<MailOutbox> list(){return repo.findAll(org.springframework.data.domain.Sort.by("id").descending());}
}
