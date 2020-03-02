package kunzou.me.scheduler.services;

import static java.time.temporal.TemporalAdjusters.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import kunzou.me.scheduler.domains.Schedule;
import kunzou.me.scheduler.domains.ScheduleEventsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ginsberg.timestream.LocalDateStream;

import kunzou.me.scheduler.domains.ScheduleEvent;
import kunzou.me.scheduler.exception.TimeNotAvailableException;

@Service
public class EventService {
  private static final String SLOT_NOT_AVAILABLE = "exception.timeNotAvailable";
  private static final Logger logger = LoggerFactory.getLogger(EventService.class);

  private MongoTemplate mongoTemplate;

  public EventService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public ResponseEntity add(ScheduleEvent scheduleEvent) {
    if(!scheduleEvent.isNew() && Objects.requireNonNull(mongoTemplate.findById(scheduleEvent.getId(), ScheduleEvent.class)).getAvailable() == 0) {
      throw new TimeNotAvailableException(SLOT_NOT_AVAILABLE);
    }
    scheduleEvent.setAvailable(scheduleEvent.getAvailable() - 1);
    mongoTemplate.save(scheduleEvent);
    return ResponseEntity.ok().build();
  }

  List<ScheduleEvent> getEventsByScheduleId(String scheduleId) {
    return mongoTemplate.find(Query.query(Criteria.where("scheduleId").is(scheduleId)), ScheduleEvent.class);
  }

  Collection<ScheduleEvent> findEventsBetween(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
    return mongoTemplate.find(new Query(Criteria.where("start").gte(startDateTime).and("end").lte(endDateTime)), ScheduleEvent.class);
  }

  List<ScheduleEvent> createCalendarEventsForUser(Schedule schedule, LocalDate startDate, LocalDate endDate) {
    return createCalendarEventsBetween(startDate, endDate, schedule.getOpenHour(), schedule.getCloseHour(), schedule.getEventInterval(), schedule.getAvailability());
  }

  public ScheduleEvent getReservedEvent(Collection<ScheduleEvent> reservedEvents, ScheduleEvent event) {
    return reservedEvents.stream()
        .filter(reservedEvent -> reservedEvent.getStart().isEqual(event.getStart()) && reservedEvent.getEnd().isEqual(event.getEnd()))
        .findAny()
        .orElse(event);
  }

  List<ScheduleEvent> createCalendarEventsBetween(LocalDate startDate, LocalDate endDate, LocalTime openTime, LocalTime closeTime, int interval, int available) {
    return LocalDateStream
      .from(startDate)
      .to(endDate)
      .stream()
      .map(date -> createCalendarEventsForDay(date, openTime, closeTime, interval, available))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  List<ScheduleEvent> createCalendarEventsForDay(LocalDate date, LocalTime startTime, LocalTime endTime, int interval, int available) {
    LocalDateTime currentStartTime = date.atTime(startTime);
    LocalDateTime currentEndTime = date.atTime(startTime).plusMinutes(interval);
    List<ScheduleEvent> events = new ArrayList<>();
    while(!currentEndTime.isAfter(date.atTime(endTime))) {
      events.add(createCalendarEvent(currentStartTime, currentEndTime, available));
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
    Collection<ScheduleEvent> reservedEvents = getEventsByScheduleId(schedule.getId());
    return createCalendarEventsBetween(
      LocalDate.now(),
      LocalDate.now().plusDays(schedule.getMaxAllowedDaysFromNow()),
      schedule.getOpenHour(), schedule.getCloseHour(), schedule.getEventInterval(), schedule.getAvailability()).stream()
      .map(event -> getReservedEvent(reservedEvents, event))
      .collect(Collectors.toList());
  }

  public ScheduleEventsResponse createScheduleResponse(String scheduleId) {
    Schedule schedule = mongoTemplate.findById(scheduleId, Schedule.class);
    ScheduleEventsResponse response = new ScheduleEventsResponse();
    List<ScheduleEvent> events = getScheduleEventsByScheduleId(schedule);
    response.setScheduleEvents(events);
    response.setDayStartHour(schedule.getOpenHour());
    response.setDayEndHour(schedule.getCloseHour());
    response.setName(schedule.getName());
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
