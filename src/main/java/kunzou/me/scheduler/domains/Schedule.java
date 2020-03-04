package kunzou.me.scheduler.domains;

import java.time.Duration;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.TimeUnit;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Schedule {
  @Id
  @Indexed(unique = true)
  private String id;
  private String userId;
  private String name;
  private int eventInterval;
  private LocalTime openHour;
  private LocalTime closeHour;
  private int maxAllowedDaysFromNow;
  private int availability;
  private String userEmail;

  public Schedule() {
  }
}
