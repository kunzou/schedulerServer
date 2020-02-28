package kunzou.me.scheduler.converters;

import java.time.Duration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToDurationConverter implements Converter<String, Duration> {
  @Override
  public Duration convert(String source) {
    String[] sourceArray = source.split(":");
    return Duration.ofHours(Long.parseLong(sourceArray[0])).plus(Duration.ofMinutes(Long.parseLong(sourceArray[1])));
  }
}
