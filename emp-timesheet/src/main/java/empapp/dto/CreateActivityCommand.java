package empapp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateActivityCommand {

    private String type;

    private LocalDate date;

    private int hour;

}
