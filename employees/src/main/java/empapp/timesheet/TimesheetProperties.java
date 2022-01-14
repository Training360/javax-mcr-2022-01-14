package empapp.timesheet;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "timesheet")
public class TimesheetProperties {

    private String url;
}
