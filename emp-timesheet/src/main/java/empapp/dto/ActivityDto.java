package empapp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ActivityDto {

    private Long id;

    private String type;

    private LocalDate date;

    private int hour;
}
