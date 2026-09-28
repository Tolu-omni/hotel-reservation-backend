package com.system.hotel_reservation;
import com.system.hotel_reservation.entity.*;
import com.system.hotel_reservation.enums.*;
import com.system.hotel_reservation.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.sql.DataSource;
import java.math.BigDecimal;
@Component @Profile("ui-preview")
public class UiPreviewData implements CommandLineRunner {
 private final RoomRepository rooms;private final CustomerRepository customers;private final UserRepository users;private final PasswordEncoder encoder;private final DataSource data;
 public UiPreviewData(RoomRepository r,CustomerRepository c,UserRepository u,PasswordEncoder e,DataSource d){rooms=r;customers=c;users=u;encoder=e;data=d;}
 @Override public void run(String... args)throws Exception{
  try(var connection=data.getConnection()){if(!connection.getMetaData().getURL().startsWith("jdbc:h2:mem:nova_preview"))throw new IllegalStateException("Preview seeding is restricted to the isolated in-memory database");}
  if(rooms.count()>0)return;
  for(int i=1;i<=6;i++){Room r=new Room(null,"10"+i,i==1?RoomType.SINGLE:i==6?RoomType.SUITE:RoomType.DOUBLE,new BigDecimal(i==6?"125000":"65000"),i==5?RoomStatus.MAINTENANCE:RoomStatus.AVAILABLE,"A calm, comfortable room prepared for your Lagos stay.");rooms.save(r);}
  for(Role role:Role.values()){User u=new User();u.setFirstName("Demo");u.setLastName(role.name());u.setEmail(role.name().toLowerCase()+"@nova.example");u.setPassword(encoder.encode("NovaPreview!2026"));u.setRole(role);users.save(u);if(role==Role.CUSTOMER){Customer c=new Customer();c.setFirstName("Demo");c.setLastName("Guest");c.setEmail(u.getEmail());c.setPhoneNumber("08000000000");customers.save(c);}}
 }
}
