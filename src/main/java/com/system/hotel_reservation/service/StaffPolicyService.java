package com.system.hotel_reservation.service;

import com.system.hotel_reservation.entity.StaffPolicy;
import com.system.hotel_reservation.repository.StaffPolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffPolicyService {

	private final StaffPolicyRepository repository;

	public StaffPolicyService(StaffPolicyRepository repository) {
		this.repository = repository;
	}

	public StaffPolicy get() {
		return repository.findById(1L).orElseGet(() -> repository.save(new StaffPolicy()));
	}

	public StaffPolicy update(StaffPolicy incoming) {
		StaffPolicy policy = get();
		policy.setCanAnswerCalls(incoming.isCanAnswerCalls());
		policy.setCanViewGuests(incoming.isCanViewGuests());
		policy.setCanCheckIn(incoming.isCanCheckIn());
		policy.setCanEditBookings(incoming.isCanEditBookings());
		policy.setCanTakePayments(incoming.isCanTakePayments());
		return repository.save(policy);
	}

	/** Throws 403 unless the current user is an admin or the capability is enabled. */
	public void require(String capability) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isAdmin = authentication != null
				&& authentication.getAuthorities().stream()
						.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
		if (isAdmin) {
			return;
		}

		StaffPolicy policy = get();
		boolean allowed = switch (capability) {
			case "answerCalls" -> policy.isCanAnswerCalls();
			case "viewGuests" -> policy.isCanViewGuests();
			case "checkIn" -> policy.isCanCheckIn();
			case "editBookings" -> policy.isCanEditBookings();
			case "takePayments" -> policy.isCanTakePayments();
			default -> true;
		};

		if (!allowed) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff permission disabled: " + capability);
		}
	}
}
