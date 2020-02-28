package kunzou.me.scheduler.configurations.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {

  @Override
  public Date convert(ZonedDateTime source) {
    return Date.from(source.toInstant());
  }
}
