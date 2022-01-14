package empapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class CreateEmployeeCommand {

    @Schema(description = "the name of the new employee", example = "John Doe")
    @NotBlank
    private String name;
}
