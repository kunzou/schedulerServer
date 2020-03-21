package kunzou.me.scheduler.domains;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Appointment {
  @Id
  @Indexed(unique = true)
  private String id;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private String scheduleId;
  private String guestFirstName;
  private String guestLastName;
  private String guestEmail;
  private String guestMessage;

  @Override
  public boolean equals(Object o) {
    return o instanceof Appointment && ((Appointment) o).getStart().equals(start) && ((Appointment) o).getEnd().equals(end);
  }

  @Override
  public int hashCode() {
    return start.hashCode() + end.hashCode();
  }

  public String getGuestName() {
    return String.join(" ", guestFirstName, guestLastName);
  }

  public String getGuestMessage() {
    return Objects.toString(guestMessage, "");
  }
}
