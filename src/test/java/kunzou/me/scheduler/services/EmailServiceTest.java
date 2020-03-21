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

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(System.class)
class EmailServiceTest {

	private EmailService emailService;

  @Mock private	MongoTemplate mongoTemplate;

	@BeforeEach
	public void setUp() {
		mongoTemplate = PowerMock.createMock(MongoTemplate.class);
		emailService = new EmailService(mongoTemplate);
	}

	@AfterEach
	public void verify() {
		PowerMock.verifyAll();
	}

	@Test
	void createEmailToGuest() throws Exception {
		Appointment appointment = createAppointment();
    Schedule schedule = createSchedule();
//		PowerMock.mockStatic(System.class);
//		EasyMock.expect(System.getenv("HOST_URL")).andReturn("https://kunzou.me");
//    todo mockstatic not working !!

		EasyMock.expect(mongoTemplate.findById("-1", Schedule.class)).andReturn(schedule);

		PowerMock.replayAll();

		Message message = emailService.createEmailToGuest(appointment);

		System.out.println(message.getSubject());
		System.out.println(message.getContent().toString());

		assertEquals("kunzou@gmail.com", Arrays.stream(message.getRecipients(Message.RecipientType.TO)).findFirst().get().toString());
	}

	private Appointment createAppointment() {
		Appointment appointment = new Appointment();
		appointment.setScheduleId("-1");
    appointment.setGuestFirstName("Kun");
    appointment.setGuestLastName("Zou");
		appointment.setGuestEmail("kunzou@gmail.com");
		appointment.setStart(ZonedDateTime.of(2020,1,1,9,30, 0, 0, ZoneId.systemDefault()));
		appointment.setEnd(ZonedDateTime.of(2020,1,1,10,30, 0, 0, ZoneId.systemDefault()));
		appointment.setGuestMessage("Good");

		return appointment;
	}

	private Schedule createSchedule() {
    Schedule schedule = new Schedule();
    schedule.setName("Booty Bay");
    schedule.setUserEmail("zoukun777@gmail.com");

    return schedule;
  }

	@Test
  public void createEmailToUser() throws Exception {
    Appointment appointment = createAppointment();
    Schedule schedule = createSchedule();

    EasyMock.expect(mongoTemplate.findById("-1", Schedule.class)).andReturn(schedule);
    PowerMock.replayAll();

    Message message = emailService.createEmailToUser(appointment);
    System.out.println(message.getSubject());
    System.out.println(message.getContent().toString());

    assertEquals("zoukun777@gmail.com", Arrays.stream(message.getRecipients(Message.RecipientType.TO)).findFirst().get().toString());
  }
}
