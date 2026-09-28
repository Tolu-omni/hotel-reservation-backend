package com.system.hotel_reservation.controller;

import com.system.hotel_reservation.dto.CallRequest;
import com.system.hotel_reservation.entity.Inquiry;
import com.system.hotel_reservation.enums.InquiryStatus;
import com.system.hotel_reservation.enums.InquiryType;
import com.system.hotel_reservation.repository.InquiryRepository;
import com.system.hotel_reservation.service.StaffPolicyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/contact")
public class InquiryController {

	private final InquiryRepository repo;
	private final StaffPolicyService staffPolicyService;
	private final Map<String, ArrayDeque<Long>> attempts = new LinkedHashMap<>();

	public InquiryController(InquiryRepository repo, StaffPolicyService staffPolicyService) {
		this.repo = repo;
		this.staffPolicyService = staffPolicyService;
	}

	private synchronized void limit(String address) {
		long now = System.currentTimeMillis();
		attempts.values().forEach(queue -> {
			while (!queue.isEmpty() && queue.peekFirst() < now - 60000) {
				queue.removeFirst();
			}
		});
		attempts.entrySet().removeIf(entry -> entry.getValue().isEmpty());
		if (attempts.size() >= 2000 && !attempts.containsKey(address)) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please retry shortly");
		}
		ArrayDeque<Long> queue = attempts.computeIfAbsent(address, key -> new ArrayDeque<>());
		if (queue.size() >= 5) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please wait a minute before sending another message");
		}
		queue.add(now);
	}

	@PostMapping
	public Inquiry create(@Valid @RequestBody Inquiry inquiry, jakarta.servlet.http.HttpServletRequest request) {
		limit(request.getRemoteAddr());
		inquiry.id = null;
		inquiry.status = InquiryStatus.NEW;
		inquiry.type = InquiryType.FORM;
		inquiry.createdAt = com.system.hotel_reservation.service.BookingService.now();
		return repo.save(inquiry);
	}

	@GetMapping
	public List<Inquiry> list() {
		staffPolicyService.require("answerCalls");
		return repo.findAll(Sort.by("createdAt").descending());
	}

	@PostMapping("/call")
	public Inquiry logCall(@Valid @RequestBody CallRequest request) {
		staffPolicyService.require("answerCalls");
		Inquiry inquiry = new Inquiry();
		inquiry.name = request.getName();
		inquiry.email = "call-" + System.currentTimeMillis() + "@nova.local";
		inquiry.phone = request.getPhone();
		inquiry.subject = request.getSubject();
		inquiry.message = request.getMessage();
		inquiry.status = InquiryStatus.NEW;
		inquiry.type = InquiryType.CALL;
		inquiry.createdAt = com.system.hotel_reservation.service.BookingService.now();
		return repo.save(inquiry);
	}

	@PatchMapping("/{id}/handled")
	public Inquiry markHandled(@PathVariable Long id) {
		staffPolicyService.require("answerCalls");
		Inquiry inquiry = repo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquiry not found"));
		inquiry.status = InquiryStatus.HANDLED;
		return repo.save(inquiry);
	}
}
