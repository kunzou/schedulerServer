package kunzou.me.scheduler.controllers;

import java.time.*;
import java.util.Collection;
import java.util.List;

import kunzou.me.scheduler.domains.Appointment;
import kunzou.me.scheduler.domains.ScheduleEventsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kunzou.me.scheduler.domains.ScheduleEvent;
import kunzou.me.scheduler.services.EventService;

@RestController
@RequestMapping("/events")
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
				30,
        2
		);
	}

  @PostMapping("")
  public ResponseEntity addReservation(@RequestBody Appointment appointment) {
    return eventService.add(appointment);
  }

  @GetMapping("/{scheduleId}")
  public ScheduleEventsResponse getScheduleEventsByScheduleId(@PathVariable("scheduleId") String scheduleId) {
	  return eventService.createScheduleResponse(scheduleId);
  }

}
