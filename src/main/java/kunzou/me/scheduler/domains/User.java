package kunzou.me.scheduler.domains;

import java.time.LocalTime;
import java.time.temporal.TemporalAmount;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.mongodb.core.index.Indexed;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {
  @Id
  @Indexed(unique = true)
  private String id;
  private String name;
  private TemporalAmount calendarInterval;
  private LocalTime openTime;
  private LocalTime closeTime;
  private TemporalAmount maxAllowedDaysFromNow;
  private int numberOfEventUnit;
}
