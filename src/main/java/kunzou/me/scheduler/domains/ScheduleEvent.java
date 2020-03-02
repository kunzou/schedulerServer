package kunzou.me.scheduler.domains;

import java.time.ZonedDateTime;

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
	@Id
	@Indexed(unique = true)
	private String id;
	private ZonedDateTime start;
	private ZonedDateTime end;
	private String scheduleId;
	private String userId;
  private int available;

  public boolean isNew() {
    return id == null;
  }
}
