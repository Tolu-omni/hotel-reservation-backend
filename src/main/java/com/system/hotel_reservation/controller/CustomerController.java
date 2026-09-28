package com.system.hotel_reservation.controller;

import com.system.hotel_reservation.entity.Customer;
import com.system.hotel_reservation.service.CustomerService;
import com.system.hotel_reservation.service.StaffPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerService customerService;
	private final StaffPolicyService staffPolicyService;

	public CustomerController(CustomerService customerService, StaffPolicyService staffPolicyService) {
		this.customerService = customerService;
		this.staffPolicyService = staffPolicyService;
	}

	@PostMapping
	public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
		return ResponseEntity.ok(customerService.createCustomer(customer));
	}

	@GetMapping
	public ResponseEntity<List<Customer>> getAllCustomers() {
		staffPolicyService.require("viewGuests");
		return ResponseEntity.ok(customerService.getAllCustomers());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
		return ResponseEntity.of(customerService.getCustomerById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(@PathVariable Long id,
			@RequestBody Customer customer) {
		return ResponseEntity.ok(customerService.updateCustomer(id, customer));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
		customerService.deleteCustomer(id);
		return ResponseEntity.noContent().build();
	}
}
