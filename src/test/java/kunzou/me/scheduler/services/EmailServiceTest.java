package kunzou.me.scheduler.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import javax.mail.Message;

import org.easymock.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import kunzou.me.scheduler.domains.Appointment;
import kunzou.me.scheduler.domains.Schedule;

@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class})
class EmailServiceTest {

	private EmailService emailService;

  @Mock private	MongoTemplate mongoTemplate;

	@BeforeEach
	public void setUp() {
//		mongoTemplate = PowerMock.createMock(MongoTemplate.class);
		emailService = new EmailService(mongoTemplate);
	}

	@AfterEach
	public void verify() {
		PowerMock.verifyAll();
	}

	@Test
	void createEmailToGuest() throws Exception {
		Appointment appointment = createAppointment();
		Schedule schedule = new Schedule();
		PowerMock.mockStatic(System.class);
		EasyMock.expect(System.getenv("HOST_URL")).andReturn("https://kunzou.me");

		EasyMock.expect(mongoTemplate.findById("-1", Schedule.class)).andReturn(schedule);

		PowerMock.replay(mongoTemplate);


		Message message = emailService.createEmailToGuest(appointment);

		System.out.println(message.getSubject());
		System.out.println(message.getContent().toString());

		assertEquals("kunzou@gmail.com", Arrays.asList(message.getRecipients(Message.RecipientType.TO)).stream().findFirst().get().toString());
	}

	private Appointment createAppointment() {
		Appointment appointment = new Appointment();
		appointment.setScheduleId("-1");
		appointment.setGuestEmail("kunzou@gmail.com");
		appointment.setStart(ZonedDateTime.of(2020,1,1,9,30, 0, 0, ZoneId.systemDefault()));
		appointment.setEnd(ZonedDateTime.of(2020,1,1,10,30, 0, 0, ZoneId.systemDefault()));

		return appointment;
	}

	private Schedule createSchedule() {
		Schedule schedule = new Schedule();
		schedule.setUserEmail("zoukun777@gmail.com");

		return schedule;
	}
}
