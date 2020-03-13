package kunzou.me.scheduler.services;

import kunzou.me.scheduler.domains.Appointment;
import kunzou.me.scheduler.domains.Schedule;
import kunzou.me.scheduler.exception.EmailServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;

@Service
public class EmailService {
  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
  private MongoTemplate mongoTemplate;
  private static final String EMAIL_SUBJECT = "New appointment: %s to %s, %s";
  private static final String GUEST_EMAIL_BODY =
      "Hello %s,\n" +
          "\t\tYour appointment with %s at %s is pending. You will be notified once it is approved. Thank you.\n\t" +
          "To review your appoint, please visit %s";

  private static final String USER_EMAIL_BODY = "Guest name: %s\nEmail: %s\nMessage: %s\nTo approve or decline this appointment, please visit: %s";

  public EmailService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public void notifyScheduleOwner(Appointment appointment) throws MessagingException {
    Transport.send(createEmailToUser(appointment));
  }

  public void notifyGuest(Appointment appointment) throws MessagingException {
    Transport.send(createEmailToGuest(appointment));
  }

  Message createEmailToGuest(Appointment appointment) throws MessagingException {
    Message message = createEmailableMessage();
    String to = appointment.getGuestEmail();
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    message.setSubject(formatEmailSubject(appointment));
    message.setText(createGuestEmailBody(appointment));

    return message;
  }

  private String createGuestEmailBody(Appointment appointment) {
    return String.format(GUEST_EMAIL_BODY,
        appointment.getGuestFirstName(),
        Optional.ofNullable(mongoTemplate.findById(appointment.getScheduleId(), Schedule.class)).map(Schedule::getName).orElse("us"),
        appointment.getStart().format(DateTimeFormatter.ofPattern("HH:mm, E, MMM dd, yyyy z")),
        getCalendarLink(appointment)
    );
  }

  private String createUserEmailBody(Appointment appointment) {
    return String.format(USER_EMAIL_BODY,
        appointment.getGuestName(),
        appointment.getGuestEmail(),
        appointment.getGuestMessage(),
        getCalendarLink(appointment)
    );
  }

  private Message createEmailToUser(Appointment appointment) throws MessagingException {
    Message message = createEmailableMessage();
    String to = Optional.ofNullable(mongoTemplate.findById(appointment.getScheduleId(), Schedule.class))
        .map(Schedule::getUserEmail)
        .orElseThrow(() -> new EmailServiceException("Cannot find user email"));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    message.setSubject(formatEmailSubject(appointment));
    message.setText(createUserEmailBody(appointment));

    return message;
  }

  private String getCalendarLink(Appointment appointment) {
    return String.join(System.getenv("HOST_URL"), "/calendar/", appointment.getScheduleId());
  }

  private String formatEmailSubject(Appointment appointment) {
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E, MMM dd,yyyy z");
    return String.format(EMAIL_SUBJECT,
        appointment.getStart().format(timeFormat),
        appointment.getEnd().format(timeFormat),
        appointment.getStart().format(dateFormat));
  }

  private Message createEmailableMessage() throws javax.mail.MessagingException {
    final String username = System.getenv("ADMIN_EMAIL");
    final String password = System.getenv("ADMIN_EMAIL_PASSWORD");

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
    message.setFrom(new InternetAddress(username));

    return message;
  }

}
