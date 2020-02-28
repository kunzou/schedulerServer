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
	private String userId;
	private String appId;
	private boolean taken;
	private int totalUnits;
	private int unitTaken;
}
