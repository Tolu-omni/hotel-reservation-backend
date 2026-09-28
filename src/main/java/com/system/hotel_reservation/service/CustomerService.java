package com.system.hotel_reservation.service;

import com.system.hotel_reservation.entity.Customer;
import com.system.hotel_reservation.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	public Customer createCustomer(Customer customer) {
		return customerRepository.save(customer);
	}

	public List<Customer> getAllCustomers() {
		return customerRepository.findAll();
	}

	public Optional<Customer> getCustomerById(Long id) {
		return customerRepository.findById(id);
	}

	public Customer updateCustomer(Long id, Customer customer) {
        Customer current=customerRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Customer not found"));
        if(customer.getEmail()!=null && !customer.getEmail().equalsIgnoreCase(current.getEmail()))throw new IllegalArgumentException("Account email cannot be changed here");
        if(customer.getFirstName()!=null){if(customer.getFirstName().isBlank()||customer.getFirstName().length()>100)throw new IllegalArgumentException("Valid first name required");current.setFirstName(customer.getFirstName().trim());}
        if(customer.getLastName()!=null){if(customer.getLastName().isBlank()||customer.getLastName().length()>100)throw new IllegalArgumentException("Valid last name required");current.setLastName(customer.getLastName().trim());}
        if(customer.getPhoneNumber()!=null){if(customer.getPhoneNumber().length()>100)throw new IllegalArgumentException("Phone too long");current.setPhoneNumber(customer.getPhoneNumber());}
        if(customer.getAddress()!=null){if(customer.getAddress().length()>300)throw new IllegalArgumentException("Address too long");current.setAddress(customer.getAddress());}
        return customerRepository.save(current);
	}

	public void deleteCustomer(Long id) {
		customerRepository.deleteById(id);
	}
}
