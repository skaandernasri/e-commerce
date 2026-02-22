package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")

public interface DateMapper {
  DateMapper INSTANCE = Mappers.getMapper(DateMapper.class);

  // From DTO (OffsetDateTime in UTC) to entity (LocalDateTime in UTC)
  default LocalDateTime mapOffsetDateTimeToLocalDateTime(OffsetDateTime date) {
    if (date == null) {
      return null;
    }
    // Convert to UTC, then get LocalDateTime without zone info
    return date.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }

  // From entity (LocalDateTime UTC) to DTO (OffsetDateTime UTC)
  default OffsetDateTime mapDateToOffsetDateTime(LocalDateTime date) {
    if (date == null) {
      return null;
    }
    // Treat LocalDateTime as UTC and convert to OffsetDateTime UTC
    return date.atOffset(ZoneOffset.UTC);
  }

  default String mapUtcDateTimeTimeToString(LocalDateTime date) {
    if (date == null) {
      return null;
    }
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'UTC'"));
  }

  default String mapOffsetDateTimeToString(OffsetDateTime date) {
    if (date == null) {
      return null;
    }
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'UTC'"));
  }
}
