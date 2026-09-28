package com.system.hotel_reservation.entity;

import com.system.hotel_reservation.enums.InquiryStatus;
import com.system.hotel_reservation.enums.InquiryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_inquiries")
public class Inquiry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	@NotBlank
	@Size(max = 100)
	public String name;

	@NotBlank
	@Email
	@Size(max = 254)
	public String email;

	@Size(max = 40)
	public String phone;

	@NotBlank
	@Size(max = 100)
	public String subject;

	@NotBlank
	@Size(min = 10, max = 4000)
	@Column(length = 4000)
	public String message;

	@Enumerated(EnumType.STRING)
	public InquiryStatus status = InquiryStatus.NEW;

	@Enumerated(EnumType.STRING)
	public InquiryType type = InquiryType.FORM;

	public LocalDateTime createdAt;
}
