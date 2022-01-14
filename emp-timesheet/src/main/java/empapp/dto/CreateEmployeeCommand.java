package empapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeCommand {

    @Schema(description = "the name of the new employee", example = "John Doe")
    private String name;

    private long eid;
}
