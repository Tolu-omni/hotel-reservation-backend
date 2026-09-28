package com.system.hotel_reservation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.hotel_reservation.dto.AuthResponse;
import com.system.hotel_reservation.dto.LoginRequest;
import com.system.hotel_reservation.dto.RegisterRequest;
import com.system.hotel_reservation.entity.Customer;
import com.system.hotel_reservation.entity.User;
import com.system.hotel_reservation.enums.Role;
import com.system.hotel_reservation.repository.CustomerRepository;
import com.system.hotel_reservation.repository.UserRepository;
import com.system.hotel_reservation.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String googleClientId;
	private final HttpClient httpClient = HttpClient.newHttpClient();

	public AuthService(UserRepository userRepository,
			CustomerRepository customerRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			@Value("${google.client-id:}") String googleClientId) {
		this.userRepository = userRepository;
		this.customerRepository = customerRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.googleClientId = googleClientId;
	}

	public AuthResponse register(RegisterRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Registration request cannot be null");
		}
		if (request.getEmail() == null || request.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new IllegalArgumentException("Password is required");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email is already registered");
		}

		User user = new User();
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.CUSTOMER);

		User savedUser = userRepository.save(user);
		ensureCustomerProfile(savedUser);
		return new AuthResponse(
				savedUser.getId(),
				savedUser.getFirstName(),
				savedUser.getLastName(),
				savedUser.getEmail(),
				savedUser.getRole());
	}

	public AuthResponse login(LoginRequest request) {
		if (request == null || request.getEmail() == null
				|| request.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new IllegalArgumentException("Password is required");
		}

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid email or password"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Invalid email or password");
		}

		return authResponseWithToken(user);
	}

	public AuthResponse loginWithGoogleToken(String credential) {
		if (googleClientId == null || googleClientId.isBlank()) {
			throw new IllegalArgumentException("Google login is not configured");
		}
		Map<String, String> claims = verifyGoogleCredential(credential);
		String email = claims.get("email");
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Google account email is required");
		}
		if (!"true".equalsIgnoreCase(claims.get("email_verified"))) {
			throw new IllegalArgumentException("Google account email is not verified");
		}

		User user = userRepository.findByEmail(email)
				.orElseGet(() -> createGoogleCustomerUser(claims, email));
		ensureCustomerProfile(user);
		return authResponseWithToken(user);
	}

	private Map<String, String> verifyGoogleCredential(String credential) {
		if (credential == null || credential.isBlank()) {
			throw new IllegalArgumentException("Google credential is required");
		}
		try {
			String encodedToken = URLEncoder.encode(credential, StandardCharsets.UTF_8);
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken))
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request,
					HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new IllegalArgumentException("Google credential is invalid");
			}
			Map<String, String> claims = objectMapper.readValue(response.body(),
					new TypeReference<>() {
					});
			if (!googleClientId.equals(claims.get("aud"))) {
				throw new IllegalArgumentException("Google credential audience is invalid");
			}
			return claims;
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unable to verify Google credential");
		}
	}

	private User createGoogleCustomerUser(Map<String, String> claims, String email) {
		String givenName = claims.get("given_name");
		String familyName = claims.get("family_name");
		String fullName = claims.get("name");

		if ((givenName == null || givenName.isBlank()) && fullName != null) {
			String[] parts = fullName.trim().split("\\s+", 2);
			givenName = parts.length > 0 ? parts[0] : "Google";
			familyName = parts.length > 1 ? parts[1] : "User";
		}

		User user = new User();
		user.setFirstName(givenName == null || givenName.isBlank() ? "Google" : givenName);
		user.setLastName(familyName == null || familyName.isBlank() ? "User" : familyName);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode("GOOGLE_OAUTH_" + UUID.randomUUID()));
		user.setRole(Role.CUSTOMER);
		return userRepository.save(user);
	}

	private void ensureCustomerProfile(User user) {
		if (user.getRole() == Role.CUSTOMER
				&& customerRepository.findByEmail(user.getEmail()).isEmpty()) {
			Customer customer = new Customer();
			customer.setFirstName(user.getFirstName());
			customer.setLastName(user.getLastName());
			customer.setEmail(user.getEmail());
			customerRepository.save(customer);
		}
	}

	private AuthResponse authResponseWithToken(User user) {
		String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
		return new AuthResponse(
				user.getId(),
				user.getFirstName(),
				user.getLastName(),
				user.getEmail(),
				user.getRole(),
				token);
	}
}
