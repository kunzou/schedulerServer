package kunzou.me.scheduler.domains;

import java.time.ZonedDateTime;
import java.util.Comparator;

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
public class ScheduleEvent {
	private ZonedDateTime start;
	private ZonedDateTime end;
	private String scheduleId;
  private int available;

  public static final Comparator<ScheduleEvent> START_DATE = Comparator.comparing(ScheduleEvent::getStart, Comparator.reverseOrder());

}
