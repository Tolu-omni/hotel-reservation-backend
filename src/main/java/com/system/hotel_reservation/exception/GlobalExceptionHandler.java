package com.system.hotel_reservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
 public ResponseEntity<Map<String,Object>> status(org.springframework.web.server.ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Request could not be completed":e.getReason()));}
 @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
 public ResponseEntity<Map<String,Object>> denied(){return ResponseEntity.status(403).body(Map.of("message","You do not have access to this resource"));}
 @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String,Object>> invalid(org.springframework.web.bind.MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("message","Check the form fields and try again"));}
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
			IllegalArgumentException exception) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("timestamp", Instant.now());
		response.put("status", HttpStatus.BAD_REQUEST.value());
		response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
		response.put("message", exception.getMessage());

		return ResponseEntity.badRequest().body(response);
	}
}
