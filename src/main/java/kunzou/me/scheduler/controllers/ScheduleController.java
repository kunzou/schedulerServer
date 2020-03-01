package kunzou.me.scheduler.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  @GetMapping("/delete")
  public void delete() {
    scheduleService.deleteSchedules();
  }


}
