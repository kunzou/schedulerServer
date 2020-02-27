package kunzou.me.scheduler.services;

import static java.time.temporal.TemporalAdjusters.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ginsberg.timestream.LocalDateStream;

import kunzou.me.scheduler.domains.ScheduleEvent;
import kunzou.me.scheduler.domains.User;
import kunzou.me.scheduler.exception.TimeNotAvailableException;

@Service
public class EventService {
  private static final String SLOT_NOT_AVAILABLE = "exception.timeNotAvailable";
  private static final Logger logger = LoggerFactory.getLogger(EventService.class);

  private MongoTemplate mongoTemplate;

  public EventService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Collection<ScheduleEvent> getAllEvents() {
    return mongoTemplate.findAll(ScheduleEvent.class);
  }

  public ResponseEntity add(ScheduleEvent calendarEvent) {
    Collection<ScheduleEvent> events = findEventsBetween(calendarEvent.getStart(), calendarEvent.getEnd());
    if(!events.isEmpty() && !hasUnitAvailable(events)) {
      throw new TimeNotAvailableException(SLOT_NOT_AVAILABLE);
    }
    calendarEvent.setUnitTaken(calendarEvent.getUnitTaken()+1);
    mongoTemplate.save(calendarEvent);

    return ResponseEntity.ok().build();
  }

  boolean hasUnitAvailable(Collection<ScheduleEvent> events) {
    return events.stream().anyMatch(this::hasUnitAvailable);
  }

  boolean hasUnitAvailable(ScheduleEvent event) {
    return event.getTotalUnits() > event.getUnitTaken();
  }

  public Collection<ScheduleEvent> getWeeklyBookedEvents(LocalDateTime currentDate) {
    LocalDateTime startDate = currentDate.with(DayOfWeek.MONDAY);
    LocalDateTime endDate = currentDate.with(DayOfWeek.FRIDAY); //todo: to end of day
    return findEventsBetween(startDate, endDate);
  }

  public Collection<ScheduleEvent> getMonthlyBookedEvents(LocalDateTime currentDate) {
    LocalDateTime startDate = currentDate.with(firstDayOfMonth());
    LocalDateTime endDate = currentDate.with(lastDayOfMonth());//todo: to end of day
    return findEventsBetween(startDate, endDate);
  }

  Collection<ScheduleEvent> findEventsBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return mongoTemplate.find(new Query(Criteria.where("start").gte(startDateTime).and("end").lte(endDateTime)), ScheduleEvent.class);
  }

  List<ScheduleEvent> createCalendarEventsForUser(String userId, LocalDate startDate, LocalDate endDate) {
    return createCalendarEventsForUser(mongoTemplate.findById(userId, User.class), startDate, endDate);
  }

  List<ScheduleEvent> createCalendarEventsForUser(User user, LocalDate startDate, LocalDate endDate) {
    return createCalendarEventsBetween(startDate, endDate, user.getOpenTime(), user.getCloseTime(), user.getCalendarInterval());
  }

  List<ScheduleEvent> createCalendarEventsForWeek(LocalDate currentDate, User user) {
    LocalDate startDate = currentDate.with(DayOfWeek.MONDAY);
    LocalDate endDate = currentDate.with(DayOfWeek.FRIDAY);
    return createCalendarEventsBetween(startDate, endDate, user.getOpenTime(), user.getCloseTime(), user.getCalendarInterval());
  }

  List<ScheduleEvent> createCalendarEventsForMonth(LocalDate currentDate, User user) {
    LocalDate startDate = currentDate.with(firstDayOfMonth());
    LocalDate endDate = currentDate.with(lastDayOfMonth());
    return createCalendarEventsBetween(startDate, endDate, user.getOpenTime(), user.getCloseTime(), user.getCalendarInterval());
  }

  public List<ScheduleEvent> getDummyEvents(LocalDate startDate, LocalDate endDate, LocalTime openTime, LocalTime closeTime, TemporalAmount interval) {
    Collection<ScheduleEvent> reservedEvents = getAllEvents();
    return createCalendarEventsBetween(startDate, endDate, openTime, closeTime, interval).stream()
        .map(event -> getReservedEvent(reservedEvents, event))
        .collect(Collectors.toList());
  }

  public ScheduleEvent getReservedEvent(Collection<ScheduleEvent> reservedEvents, ScheduleEvent event) {
    return reservedEvents.stream()
        .filter(reservedEvent -> reservedEvent.getStart().isEqual(event.getStart()) && reservedEvent.getEnd().isEqual(event.getEnd()))
        .findAny()
        .orElse(event);
  }

  List<ScheduleEvent> createCalendarEventsBetween(LocalDate startDate, LocalDate endDate, LocalTime openTime, LocalTime closeTime, TemporalAmount interval) {
    return LocalDateStream
      .from(startDate)
      .to(endDate)
      .stream()
      .map(date -> createCalendarEventsForDay(date, openTime, closeTime, interval))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  List<ScheduleEvent> createCalendarEventsForDay(LocalDate date, LocalTime startTime, LocalTime endTime, TemporalAmount interval) {
    LocalDateTime currentEndTime = date.atTime(startTime).plus(interval);

    LocalDateTime currentStartTime = date.atTime(startTime);
    List<ScheduleEvent> events = new ArrayList<>();
    while(!currentEndTime.isAfter(date.atTime(endTime))) {
      events.add(createCalendarEvent(currentStartTime, currentEndTime));
      currentStartTime = currentEndTime;
      currentEndTime = currentEndTime.plus(interval);
    }

    return events;
  }

  ScheduleEvent createCalendarEvent(LocalDateTime start, LocalDateTime end) {
    ScheduleEvent calendarEvent = new ScheduleEvent();
    calendarEvent.setStart(start);
    calendarEvent.setEnd(end);
    calendarEvent.setTotalUnits(2); //todo
    return calendarEvent;
  }

  //dummy
  public void delete() {
    getAllEvents().forEach(event -> {
      mongoTemplate.remove(event);
    });
  }
}
