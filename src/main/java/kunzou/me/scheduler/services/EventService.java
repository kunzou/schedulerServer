package kunzou.me.scheduler.services;

import static java.time.temporal.TemporalAdjusters.*;

import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    Collection<ScheduleEvent> events = findEventsBetween(scheduleEvent.getStart(), scheduleEvent.getEnd());
    if(!events.isEmpty() && !hasUnitAvailable(events)) {
      throw new TimeNotAvailableException(SLOT_NOT_AVAILABLE);
    }
    scheduleEvent.setUnitTaken(scheduleEvent.getUnitTaken()+1);
    mongoTemplate.save(scheduleEvent);
    return ResponseEntity.ok().build();
  }

  List<ScheduleEvent> getEventsByScheduleId(String scheduleId) {
    return mongoTemplate.find(Query.query(Criteria.where("scheduleId").is(scheduleId)), ScheduleEvent.class);
  }

  boolean hasUnitAvailable(Collection<ScheduleEvent> events) {
    return events.stream().anyMatch(this::hasUnitAvailable);
  }

  boolean hasUnitAvailable(ScheduleEvent event) {
    return event.getTotalUnits() > event.getUnitTaken();
  }

  public Collection<ScheduleEvent> getWeeklyBookedEvents(ZonedDateTime currentDate) {
    ZonedDateTime startDate = currentDate.with(DayOfWeek.MONDAY);
    ZonedDateTime endDate = currentDate.with(DayOfWeek.FRIDAY); //todo: to end of day
    return findEventsBetween(startDate, endDate);
  }

  public Collection<ScheduleEvent> getMonthlyBookedEvents(ZonedDateTime currentDate) {
    ZonedDateTime startDate = currentDate.with(firstDayOfMonth());
    ZonedDateTime endDate = currentDate.with(lastDayOfMonth());//todo: to end of day
    return findEventsBetween(startDate, endDate);
  }

  Collection<ScheduleEvent> findEventsBetween(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
    return mongoTemplate.find(new Query(Criteria.where("start").gte(startDateTime).and("end").lte(endDateTime)), ScheduleEvent.class);
  }

  List<ScheduleEvent> createCalendarEventsForUser(Schedule schedule, LocalDate startDate, LocalDate endDate) {
    return createCalendarEventsBetween(startDate, endDate, schedule.getOpenHour(), schedule.getCloseHour(), schedule.getEventInterval());
  }

  List<ScheduleEvent> createCalendarEventsForWeek(LocalDate currentDate, Schedule schedule) {
    LocalDate startDate = currentDate.with(DayOfWeek.MONDAY);
    LocalDate endDate = currentDate.with(DayOfWeek.FRIDAY);
    return createCalendarEventsBetween(startDate, endDate, schedule.getOpenHour(), schedule.getCloseHour(), schedule.getEventInterval());
  }

  List<ScheduleEvent> createCalendarEventsForMonth(LocalDate currentDate, Schedule schedule) {
    LocalDate startDate = currentDate.with(firstDayOfMonth());
    LocalDate endDate = currentDate.with(lastDayOfMonth());
    return createCalendarEventsBetween(startDate, endDate, schedule.getOpenHour(), schedule.getCloseHour(), schedule.getEventInterval());
  }

  public ScheduleEvent getReservedEvent(Collection<ScheduleEvent> reservedEvents, ScheduleEvent event) {
    return reservedEvents.stream()
        .filter(reservedEvent -> reservedEvent.getStart().isEqual(event.getStart()) && reservedEvent.getEnd().isEqual(event.getEnd()))
        .findAny()
        .orElse(event);
  }

  List<ScheduleEvent> createCalendarEventsBetween(LocalDate startDate, LocalDate endDate, LocalTime openTime, LocalTime closeTime, int interval) {
    return LocalDateStream
      .from(startDate)
      .to(endDate)
      .stream()
      .map(date -> createCalendarEventsForDay(date, openTime, closeTime, interval))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  List<ScheduleEvent> createCalendarEventsForDay(LocalDate date, LocalTime startTime, LocalTime endTime, int interval) {
    LocalDateTime currentStartTime = date.atTime(startTime);
    LocalDateTime currentEndTime = date.atTime(startTime).plusMinutes(interval);
    List<ScheduleEvent> events = new ArrayList<>();
    while(!currentEndTime.isAfter(date.atTime(endTime))) {
      events.add(createCalendarEvent(currentStartTime, currentEndTime));
      currentStartTime = currentEndTime;
      currentEndTime = currentEndTime.plusMinutes(interval);
    }

    return events;
  }

  ScheduleEvent createCalendarEvent(LocalDateTime start, LocalDateTime end) {
    ScheduleEvent calendarEvent = new ScheduleEvent();
    calendarEvent.setStart(ZonedDateTime.of(start, ZoneId.systemDefault()));
    calendarEvent.setEnd(ZonedDateTime.of(end, ZoneId.systemDefault()));
    calendarEvent.setTotalUnits(2); //todo
    return calendarEvent;
  }

  List<ScheduleEvent> getScheduleEventsByScheduleId(Schedule schedule) {
//    Schedule schedule = mongoTemplate.findById(scheduleId, Schedule.class);
    Collection<ScheduleEvent> reservedEvents = getEventsByScheduleId(schedule.getId());
    return createCalendarEventsBetween(
      LocalDate.now(),
      LocalDate.now().plusDays(schedule.getMaxAllowedDaysFromNow()),
      schedule.getOpenHour(), schedule.getCloseHour(), schedule.getEventInterval()).stream()
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

  public List<ScheduleEvent> getDummyEvents(LocalDate startDate, LocalDate endDate, LocalTime openTime, LocalTime closeTime, int interval) {
    Collection<ScheduleEvent> reservedEvents = getAllEvents();
    return createCalendarEventsBetween(startDate, endDate, openTime, closeTime, interval).stream()
      .map(event -> getReservedEvent(reservedEvents, event))
      .collect(Collectors.toList());
  }

  public Collection<ScheduleEvent> getAllEvents() {
    return mongoTemplate.findAll(ScheduleEvent.class);
  }
}
