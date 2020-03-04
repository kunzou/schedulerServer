package kunzou.me.scheduler.handlers;

import javax.mail.MessagingException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice(annotations = RestController.class)
public class MessagingExceptionHandler {

	@ExceptionHandler(MessagingException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public String badRequestExceptionHandler(RestClientResponseException e) {
		return "Failed to send email. Please contact the owner";
	}
}
