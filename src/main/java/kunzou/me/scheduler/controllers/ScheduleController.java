package kunzou.me.scheduler.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kunzou.me.scheduler.domains.Schedule;
import kunzou.me.scheduler.services.ScheduleService;

@RestController
@RequestMapping("/schedule")
@CrossOrigin
public class ScheduleController {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
	private ScheduleService scheduleService;

	public ScheduleController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@GetMapping("/user/{userId}")
  public List<Schedule> getSchedulesByUserId(@PathVariable("userId") String userId) {
    return scheduleService.getSchedulesByUserId(userId);
  }

	@GetMapping("/{id}")
  public Schedule getScheduleById(@PathVariable("id") String id) {
    return scheduleService.getScheduleById(id);
  }

  @PostMapping("")
  public Schedule save(@RequestBody Schedule schedule) {
    return scheduleService.save(schedule);
  }

	@GetMapping("")
  public List<Schedule> getAllSchedules() {
    return scheduleService.getAllSchedules();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity delete(@PathVariable("id") String id) {
    scheduleService.delete(id);
    return ResponseEntity.ok().build();
  }
}
