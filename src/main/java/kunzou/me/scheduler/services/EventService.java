package kunzou.me.scheduler.services;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import kunzou.me.scheduler.domains.Appointment;
import kunzou.me.scheduler.domains.Schedule;
import kunzou.me.scheduler.domains.ScheduleEventsResponse;
import kunzou.me.scheduler.exception.TimeNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ginsberg.timestream.LocalDateStream;

import kunzou.me.scheduler.domains.ScheduleEvent;

@Service
public class EventService {
  private static final String SLOT_NOT_AVAILABLE = "exception.timeNotAvailable";
  private static final Logger logger = LoggerFactory.getLogger(EventService.class);

  private MongoTemplate mongoTemplate;
  private EmailService emailService;

  public EventService(MongoTemplate mongoTemplate, EmailService emailService) {
    this.mongoTemplate = mongoTemplate;
    this.emailService = emailService;
  }

  public ResponseEntity add(Appointment appointment) throws MessagingException {
    Schedule schedule = mongoTemplate.findById(appointment.getScheduleId(), Schedule.class);
    if(schedule == null || countAppointments(appointment) >= schedule.getAvailability()) {
      throw new TimeNotAvailableException(SLOT_NOT_AVAILABLE);
    }

    emailService.sendEmails(appointment);

    mongoTemplate.save(appointment);
    return ResponseEntity.ok().build();
  }

  int countAppointments(Appointment appointment) {
    return mongoTemplate.find(Query.query(Criteria.where(appointment.getScheduleId()).is("scheduleId").and("start").is(appointment.getStart()).and("end").is(appointment.getEnd())), Appointment.class).size();
  }

  List<Appointment> getEventsByScheduleId(String scheduleId) {
    return mongoTemplate.find(Query.query(Criteria.where("scheduleId").is(scheduleId).and("start").gte(ZonedDateTime.now())), Appointment.class);
  }

  public ScheduleEvent getReservedEvent(Collection<ScheduleEvent> reservedEvents, ScheduleEvent event) {
    return reservedEvents.stream()
        .filter(reservedEvent -> reservedEvent.getStart().isEqual(event.getStart()) && reservedEvent.getEnd().isEqual(event.getEnd()))
        .findAny()
        .orElse(event);
  }

	boolean hasOverlapDates(ZonedDateTime reservedStart, ZonedDateTime reservedEnd, ZonedDateTime scheduledStart, ZonedDateTime scheduledEnd) {
		return !(!reservedStart.isBefore(scheduledEnd) || !reservedEnd.isAfter(scheduledStart));
	}

	boolean hasOverlapDates(List<Appointment> reserved, ScheduleEvent scheduled) {
    return reserved.stream()
        .anyMatch(reservedEvent -> hasOverlapDates(reservedEvent.getStart(), reservedEvent.getEnd(), scheduled.getStart(), scheduled.getEnd()));
  }

  List<ScheduleEvent> createCalendarEventsBetween(LocalDate startDate, LocalDate endDate, LocalTime openTime, LocalTime closeTime, int interval, int available) {
    return LocalDateStream
      .from(startDate)
      .to(endDate)
      .stream()
      .map(date -> createDailyCalendarEvents(date, openTime, closeTime, interval, available))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  List<ScheduleEvent> createDailyCalendarEvents(LocalDate date, LocalTime startTime, LocalTime endTime, int interval, int available) {
    LocalDateTime currentStartTime = date.atTime(startTime);
    LocalDateTime currentEndTime = date.atTime(startTime).plusMinutes(interval);
    List<ScheduleEvent> events = new ArrayList<>();
    while(!currentEndTime.isAfter(date.atTime(endTime))) {
      if(currentStartTime.isAfter(LocalDateTime.now())) {
        events.add(createCalendarEvent(currentStartTime, currentEndTime, available));
      }
      currentStartTime = currentEndTime;
      currentEndTime = currentEndTime.plusMinutes(interval);
    }

    return events;
  }

  ScheduleEvent createCalendarEvent(LocalDateTime start, LocalDateTime end, int available) {
    ScheduleEvent calendarEvent = new ScheduleEvent();
    calendarEvent.setStart(ZonedDateTime.of(start, ZoneId.systemDefault()));
    calendarEvent.setEnd(ZonedDateTime.of(end, ZoneId.systemDefault()));
    calendarEvent.setAvailable(available);
    return calendarEvent;
  }

  List<ScheduleEvent> getScheduleEventsByScheduleId(Schedule schedule) {
    List<Appointment> reservedEvents = getEventsByScheduleId(schedule.getId());
    List<ScheduleEvent> createdEvents = createCalendarEventsBetween(
      LocalDate.now(),
      LocalDate.now().plusDays(schedule.getMaxAllowedDaysFromNow()),
      schedule.getOpenHour(), schedule.getCloseHour(), schedule.getEventInterval(), schedule.getAvailability()).stream()
        .filter(scheduledEvent -> !hasOverlapDates(reservedEvents, scheduledEvent))
        .collect(Collectors.toList());
    return Stream.concat(createdEvents.stream(), createScheduleEvents(reservedEvents, schedule.getAvailability()).stream())
        .collect(Collectors.toList());
  }

  List<ScheduleEvent> createScheduleEvents(List<Appointment> appointments, int available) {
    List<ScheduleEvent> scheduleEvents = new ArrayList<>();
    appointments.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
      .forEach((appointment, count) -> {
        ScheduleEvent scheduleEvent = createScheduleEvent(appointment);
        scheduleEvent.setAvailable(available - count.intValue());
        scheduleEvents.add(scheduleEvent);
      });
    return scheduleEvents;
  }

  ScheduleEvent createScheduleEvent(Appointment appointment) {
    ScheduleEvent scheduleEvent = new ScheduleEvent();
    scheduleEvent.setStart(appointment.getStart());
    scheduleEvent.setEnd(appointment.getEnd());
    return scheduleEvent;
  }

  public ScheduleEventsResponse createScheduleResponse(String scheduleId) {
    Schedule schedule = mongoTemplate.findById(scheduleId, Schedule.class);
    ScheduleEventsResponse response = new ScheduleEventsResponse();
    if(schedule.isCreatable()) {
      List<ScheduleEvent> events = getScheduleEventsByScheduleId(schedule);
      response.setScheduleEvents(events);
      response.setDayStartHour(schedule.getOpenHour());
      response.setDayEndHour(schedule.getCloseHour());
      response.setName(schedule.getName());
    }

    return response;
  }

  //dummies
  public void delete() {
    getAllEvents().forEach(event -> {
      mongoTemplate.remove(event);
    });
  }

  public List<ScheduleEvent> getDummyEvents(LocalDate startDate, LocalDate endDate, LocalTime openTime, LocalTime closeTime, int interval, int available) {
    Collection<ScheduleEvent> reservedEvents = getAllEvents();
    return createCalendarEventsBetween(startDate, endDate, openTime, closeTime, interval, available).stream()
      .map(event -> getReservedEvent(reservedEvents, event))
      .collect(Collectors.toList());
  }

  public Collection<ScheduleEvent> getAllEvents() {
    return mongoTemplate.findAll(ScheduleEvent.class);
  }
}
