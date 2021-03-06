package kunzou.me.scheduler.configurations;

import kunzou.me.scheduler.converters.*;
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

  public MongoTemplateConfig(DateToZonedDateTimeConverter dateToZonedDateTimeConverter, ZonedDateTimeToDateConverter zonedDateTimeToDateConverter) {
    this.dateToZonedDateTimeConverter = dateToZonedDateTimeConverter;
    this.zonedDateTimeToDateConverter = zonedDateTimeToDateConverter;
  }

  @Bean
  public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory, MongoMappingContext context) {
    MongoCustomConversions conversions = new MongoCustomConversions(
        Arrays.asList(
            dateToZonedDateTimeConverter,
            zonedDateTimeToDateConverter
        ));
    MappingMongoConverter converter =
      new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), context);
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    converter.setCustomConversions(conversions);

    converter.afterPropertiesSet();
    return new MongoTemplate(mongoDbFactory, converter);
  }
}
