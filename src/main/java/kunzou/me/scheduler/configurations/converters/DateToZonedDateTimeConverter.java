package kunzou.me.scheduler.configurations.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

import static java.time.ZoneId.systemDefault;
import static java.time.ZonedDateTime.ofInstant;

@Component
public class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
  @Override
  public ZonedDateTime convert(Date source) {
    return ofInstant(source.toInstant(), systemDefault());
  }
}
