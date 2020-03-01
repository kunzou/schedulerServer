package kunzou.me.scheduler.domains;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ScheduleEventsResponse {
  String name;
  LocalTime dayStartHour;
  LocalTime dayEndHour;
  List<ScheduleEvent> scheduleEvents;
}
