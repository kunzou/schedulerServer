package kunzou.me.scheduler.controllers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kunzou.me.scheduler.domains.ScheduleEvent;
import kunzou.me.scheduler.services.EventService;

@RestController
@RequestMapping("/calendar")
@CrossOrigin
public class EventController {
	private static final Logger logger = LoggerFactory.getLogger(EventController.class);

	private EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@GetMapping("/reserved")
	public Collection<ScheduleEvent> getReserved() {
		return eventService.getAllEvents();
	}

	@GetMapping("/delete")
	public void delete() {
		eventService.delete();
	}

	@GetMapping("")
	public List<ScheduleEvent> getDummyEvents() {
		return eventService.getDummyEvents(
				LocalDate.now(),
				LocalDate.of(2020,2,29),
				LocalTime.of(8,0),
				LocalTime.of(17,0),
				Duration.ofMinutes(30)
		);
	}

  @PostMapping("")
  public ResponseEntity addReservation(@RequestBody ScheduleEvent calendarEvent) {
    return eventService.add(calendarEvent);
  }

}
