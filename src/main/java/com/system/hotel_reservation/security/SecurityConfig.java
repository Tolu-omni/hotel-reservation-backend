package com.system.hotel_reservation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			CorsConfigurationSource corsConfigurationSource) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.sessionManagement(session -> session
						.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**")
						.permitAll()
						.requestMatchers(HttpMethod.POST,
								"/api/auth/register",
								"/api/auth/login",
								"/api/auth/google")
						.permitAll()
						.requestMatchers(HttpMethod.PATCH,
								"/api/bookings/*/confirm",
								"/api/bookings/*/check-in",
								"/api/bookings/*/check-out",
								"/api/bookings/*/cancel")
						.hasAnyRole("STAFF", "ADMIN")
						.requestMatchers(HttpMethod.GET,
								"/api/rooms", "/api/rooms/**")
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/api/rooms")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/rooms/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/rooms/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/customers/*")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/customers")
						.hasAnyRole("STAFF", "ADMIN")
						.requestMatchers(HttpMethod.PUT,
								"/api/customers/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE,
								"/api/customers/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/payments")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH,
								"/api/payments/*/success")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()
                        .requestMatchers(HttpMethod.POST,
								"/api/payments/initialize/**")
						.hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF",
								"ROLE_ADMIN")
						.requestMatchers(HttpMethod.GET,
								"/api/payments/verify/**")
						.hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF",
								"ROLE_ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/bookings/my")
						.hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/bookings")
						.hasAnyRole("STAFF", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/bookings/*")
						.authenticated()
						.requestMatchers(HttpMethod.POST, "/api/bookings")
						.authenticated()
						.requestMatchers(HttpMethod.GET, "/api/payments/**")
						.authenticated()
						.requestMatchers(HttpMethod.POST, "/api/payments")
                        .hasAnyRole("STAFF", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/bookings/*").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/contact", "/api/payments/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/contact").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/mail-preview").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/contact/call").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/contact/*/handled").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/staff-permissions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/staff-permissions").hasRole("ADMIN")
                        .anyRequest().denyAll())
				.addFilterBefore(jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(@org.springframework.beans.factory.annotation.Value("${nova.allowed-origins:http://localhost:3000,http://localhost:3100}") String origins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(java.util.Arrays.stream(origins.split(",")).map(String::trim).toList());
		configuration.setAllowedMethods(List.of(
				"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of(
				"Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source =
				new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider(
			CustomUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider =
				new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}
}
