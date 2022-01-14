package empapp.service;

import empapp.dto.ActivityDto;
import empapp.entities.Activity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    ActivityDto toDto(Activity activity);
}
