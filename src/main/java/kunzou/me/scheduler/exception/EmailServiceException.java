package kunzou.me.scheduler.exception;

import javax.mail.MessagingException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class EmailServiceException extends MessagingException {
  public EmailServiceException(String message) {
    super(message);
  }
}
