package kunzou.me.scheduler.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import kunzou.me.scheduler.domains.Schedule;
import kunzou.me.scheduler.domains.ScheduleEvent;

@Service
public class ScheduleService {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
	private MongoTemplate mongoTemplate;

	public ScheduleService(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public Schedule save(Schedule schedule) {
		return mongoTemplate.save(schedule);
	}

	public List<Schedule> getSchedulesByUserId(String userId) {
		return mongoTemplate.find(Query.query(Criteria.where("userId").is(userId)), Schedule.class);
	}

	public Schedule getScheduleById(String id) {
//		return mongoTemplate.findById(Query.query(Criteria.where("id").is(id)), Schedule.class);
		return mongoTemplate.findById(id, Schedule.class);
	}

	public List<Schedule> getAllSchedules() {
		return mongoTemplate.findAll(Schedule.class);
	}
}
