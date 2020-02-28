package kunzou.me.scheduler.converters;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;

@Component
public class DurationToStringConverter implements Converter<Duration, String> {

  @Override
  public String convert(Duration source) {
    return source.toHours()+":"+source.toMinutes();
  }
}
