package kunzou.me.scheduler.services;

import kunzou.me.scheduler.domains.Appointment;
import kunzou.me.scheduler.domains.Email;
import kunzou.me.scheduler.domains.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Set;

@Service
public class EmailService {
  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
  private MongoTemplate mongoTemplate;

  public EmailService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public void notifyScheduleOwner(Appointment appointment) throws MessagingException {
    Transport.send(createMessage(appointment));
  }

  private Message createMessage(Appointment appointment) throws MessagingException {
    Message message = createEmailableMessage(appointment);
    String to = mongoTemplate.findById(appointment.getScheduleId(), Schedule.class).getOwnerEmail();
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    message.setSubject("New appointment: "+ appointment.getStart() + appointment.getEnd());
    message.setText(
      "First name:"+appointment.getGuestFirstName()+"\n"+
      "Last name:"+appointment.getGuestLastName()+"\n"+
      "Email:"+appointment.getGuestEmail()+"\n"+
      "Message:"+appointment.getGuestMessage()+"\n"+
      "To approve or decline this appointment, please visit" +
      System.getenv("HOST_URL") + "/calendar/" + appointment.getScheduleId()
    );

    return message;
  }

  private Message createEmailableMessage(Appointment appointment) throws javax.mail.MessagingException {
    final String username = System.getenv("EMAIL_USERNAME");
    final String password = System.getenv("EMAIL_PASSWORD");

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.live.com");
    props.put("mail.smtp.debug", "true");
    props.put("mail.smtp.port", "587");

    Session session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, password);
        }
      });
    session.setDebug(false);
    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(System.getenv("EMAIL_USERNAME")));

    return message;
  }

}
