package com.system.hotel_reservation.controller;

import com.system.hotel_reservation.entity.StaffPolicy;
import com.system.hotel_reservation.service.StaffPolicyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/staff-permissions")
public class AdminStaffController {

	private final StaffPolicyService staffPolicyService;

	public AdminStaffController(StaffPolicyService staffPolicyService) {
		this.staffPolicyService = staffPolicyService;
	}

	@GetMapping
	public StaffPolicy get() {
		return staffPolicyService.get();
	}

	@PutMapping
	public StaffPolicy update(@RequestBody StaffPolicy policy) {
		return staffPolicyService.update(policy);
	}
}
