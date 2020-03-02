package kunzou.me.scheduler.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kunzou.me.scheduler.domains.ScheduleEvent;

class EventServiceTest {
  private EventService eventService;

  @BeforeEach
  public void setUp() {
    eventService = new EventService(null);
  }

  @Test
  public void createCalendarEventsForDay() {
    List<ScheduleEvent> events1 = eventService.createCalendarEventsForDay(
      LocalDate.of(2020, 1, 1),
      LocalTime.of(8, 0),
      LocalTime.of(8, 0),
      30, 2);

    List<ScheduleEvent> events2 = eventService.createCalendarEventsForDay(
      LocalDate.of(2020, 1, 1),
      LocalTime.of(8, 0),
      LocalTime.of(7, 0),
      30, 2);

    List<ScheduleEvent> events3 = eventService.createCalendarEventsForDay(
      LocalDate.of(2020, 1, 1),
      LocalTime.of(8, 0),
      LocalTime.of(9, 0),
      30, 2);

    List<ScheduleEvent> events4 = eventService.createCalendarEventsForDay(
      LocalDate.of(2020, 1, 1),
      LocalTime.of(8, 0),
      LocalTime.of(8, 59),
      30, 2);

    List<ScheduleEvent> events5 = eventService.createCalendarEventsForDay(
      LocalDate.of(2020, 1, 1),
      LocalTime.of(8, 0),
      LocalTime.of(17, 30),
      30, 2);

    List<ScheduleEvent> events6 = eventService.createCalendarEventsForDay(
      LocalDate.of(2020, 1, 1),
      LocalTime.of(8, 0),
      LocalTime.of(9, 0),
      61, 2);

    assertTrue(events1.isEmpty());
    assertTrue(events2.isEmpty());
    assertEquals(2, events3.size());
    assertEquals(1, events4.size());
    assertEquals(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 8, 30), ZoneId.systemDefault()), events4.get(0).getEnd());
    assertEquals(19, events5.size());
    assertEquals(0, events6.size());
  }
}
