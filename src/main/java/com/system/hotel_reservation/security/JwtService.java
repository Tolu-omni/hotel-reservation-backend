package com.system.hotel_reservation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long expirationMilliseconds;

	public JwtService(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration}") long expirationMilliseconds) {
		if (secret == null || secret.length() < 32) {
			throw new IllegalArgumentException(
					"JWT secret must be at least 32 characters long");
		}
		this.signingKey = Keys.hmacShaKeyFor(
				secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMilliseconds = expirationMilliseconds;
	}

	public String generateToken(String email, String role) {
		Instant issuedAt = Instant.now();
		return Jwts.builder()
				.subject(email)
				.claim("role", role)
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(
						issuedAt.plusMillis(expirationMilliseconds)))
				.signWith(signingKey)
				.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public String extractRole(String token) {
		return parseClaims(token).get("role", String.class);
	}

	public boolean isTokenValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
