package com.system.hotel_reservation.controller;

import com.system.hotel_reservation.dto.BookingRequest;
import com.system.hotel_reservation.entity.Booking;
import com.system.hotel_reservation.service.BookingService;
import com.system.hotel_reservation.service.StaffPolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

	private final BookingService bookingService;
	private final StaffPolicyService staffPolicyService;

	public BookingController(BookingService bookingService, StaffPolicyService staffPolicyService) {
		this.bookingService = bookingService;
		this.staffPolicyService = staffPolicyService;
	}

	@PostMapping
	public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		boolean isCustomer = hasAuthority(authentication, "ROLE_CUSTOMER");
		Booking booking = isCustomer
				? bookingService.createBooking(request, authentication.getName())
				: bookingService.createBooking(request);
		return ResponseEntity.ok(booking);
	}

	@GetMapping
	public ResponseEntity<List<Booking>> getAllBookings(
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAnyAuthority(authentication, "ROLE_STAFF", "ROLE_ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(bookingService.getAllBookings());
	}

	@GetMapping("/my")
	public ResponseEntity<List<Booking>> getMyBookings(
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAuthority(authentication, "ROLE_CUSTOMER")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(
				bookingService.getMyBookings(authentication.getName()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Booking> getBookingById(@PathVariable Long id,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		boolean canViewAll = hasAnyAuthority(authentication, "ROLE_STAFF",
				"ROLE_ADMIN");
		if (canViewAll) {
			return ResponseEntity.of(bookingService.getBookingById(id));
		}

		return bookingService.getBookingForCustomer(id, authentication.getName())
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN)
						.build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Booking> updateBooking(@PathVariable Long id,
			@RequestBody BookingRequest booking,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAnyAuthority(authentication, "ROLE_STAFF", "ROLE_ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		staffPolicyService.require("editBookings");
		return ResponseEntity.ok(bookingService.updateBooking(id, booking));
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<Booking> cancelBooking(@PathVariable Long id,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAnyAuthority(authentication, "ROLE_STAFF", "ROLE_ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		staffPolicyService.require("editBookings");
		return ResponseEntity.ok(bookingService.cancelBooking(id));
	}

	@PatchMapping("/{id}/confirm")
	public ResponseEntity<Booking> confirmBooking(@PathVariable Long id,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAnyAuthority(authentication, "ROLE_STAFF", "ROLE_ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		staffPolicyService.require("editBookings");
		return ResponseEntity.ok(bookingService.confirmBooking(id));
	}

	@PatchMapping("/{id}/check-in")
	public ResponseEntity<Booking> checkIn(@PathVariable Long id,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAnyAuthority(authentication, "ROLE_STAFF", "ROLE_ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		staffPolicyService.require("checkIn");
		return ResponseEntity.ok(bookingService.checkIn(id));
	}

	@PatchMapping("/{id}/check-out")
	public ResponseEntity<Booking> checkOut(@PathVariable Long id,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAnyAuthority(authentication, "ROLE_STAFF", "ROLE_ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		staffPolicyService.require("checkIn");
		return ResponseEntity.ok(bookingService.checkOut(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBooking(@PathVariable Long id,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (!hasAuthority(authentication, "ROLE_ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		bookingService.deleteBooking(id);
		return ResponseEntity.noContent().build();
	}

	private boolean hasAuthority(Authentication authentication, String authority) {
		return authentication.getAuthorities() != null
				&& authentication.getAuthorities().stream()
					.anyMatch(grantedAuthority -> authority
							.equals(grantedAuthority.getAuthority()));
	}

	private boolean hasAnyAuthority(Authentication authentication,
			String... authorities) {
		return authentication.getAuthorities() != null
				&& Arrays.stream(authorities)
					.anyMatch(authority -> authentication.getAuthorities().stream()
							.anyMatch(grantedAuthority -> authority
									.equals(grantedAuthority.getAuthority())));
	}
}
