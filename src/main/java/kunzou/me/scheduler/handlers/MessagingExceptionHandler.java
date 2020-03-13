package kunzou.me.scheduler.handlers;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import kunzou.me.scheduler.services.EmailService;

@RestControllerAdvice(annotations = RestController.class)
public class MessagingExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	@ExceptionHandler(MessagingException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ResponseBody
	public String badRequestExceptionHandler(RestClientResponseException e) {
		logger.error(e.getMessage(), e);
		return "Failed to send email. Please contact the owner";
	}
}
