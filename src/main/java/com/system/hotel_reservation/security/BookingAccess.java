package com.system.hotel_reservation.security;
import com.system.hotel_reservation.entity.Booking;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
public final class BookingAccess {
 private BookingAccess() {}
 public static void require(Booking booking) {
  var auth=SecurityContextHolder.getContext().getAuthentication();
  if(auth==null || !auth.isAuthenticated()) throw new AccessDeniedException("Sign in required");
  boolean operator=auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN")||a.getAuthority().equals("ROLE_STAFF"));
  if(!operator && (booking.getCustomer()==null || !auth.getName().equalsIgnoreCase(booking.getCustomer().getEmail()))) throw new AccessDeniedException("This booking is not accessible");
 }
}
