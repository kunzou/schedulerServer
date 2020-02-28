package kunzou.me.scheduler.configurations;

import kunzou.me.scheduler.converters.DateToZonedDateTimeConverter;
import kunzou.me.scheduler.converters.DurationToStringConverter;
import kunzou.me.scheduler.converters.StringToDurationConverter;
import kunzou.me.scheduler.converters.ZonedDateTimeToDateConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Arrays;

@Configuration
public class MongoTemplateConfig {

  private DateToZonedDateTimeConverter dateToZonedDateTimeConverter;
  private ZonedDateTimeToDateConverter zonedDateTimeToDateConverter;
  private DurationToStringConverter durationToStringConverter;
  private StringToDurationConverter stringToDurationConverter;

  public MongoTemplateConfig(DateToZonedDateTimeConverter dateToZonedDateTimeConverter, ZonedDateTimeToDateConverter zonedDateTimeToDateConverter, DurationToStringConverter durationToStringConverter, StringToDurationConverter stringToDurationConverter) {
    this.dateToZonedDateTimeConverter = dateToZonedDateTimeConverter;
    this.zonedDateTimeToDateConverter = zonedDateTimeToDateConverter;
    this.durationToStringConverter = durationToStringConverter;
    this.stringToDurationConverter = stringToDurationConverter;
  }

  @Bean
  public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory, MongoMappingContext context) {
    MongoCustomConversions conversions = new MongoCustomConversions(
        Arrays.asList(
            dateToZonedDateTimeConverter,
            zonedDateTimeToDateConverter,
            durationToStringConverter,
            stringToDurationConverter
        ));
    MappingMongoConverter converter =
      new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), context);
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    converter.setCustomConversions(conversions);

    converter.afterPropertiesSet();
    return new MongoTemplate(mongoDbFactory, converter);
  }
}
